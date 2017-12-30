(ns clj-puredata.translate
  (:require [clojure.string :as string]
            [clj-puredata.parse :refer [in-context]]
            [clj-puredata.puredata :refer [reload]]))

(def obj-nodes #{"bang" "b" "float" "f" "symbol" "int" "i" "send" "s" "receive" "r" "select" "route" "pack" "unpack" "trigger" "t" "spigot" "moses" "until" "print" "makefilename" "change" "swap" "value" "v" "list" ; general
                 "delay" "metro" "line" "timer" "cputime" "realtime" "pipe" ; time
                 "+" "-" "*" "/" "pow" "==" "!=" ">" "<" ">=" "<=" "&" "&&" "||" "||||" "%" "<<" ">>" "mtof" "powtodb" "rmstodb" "ftom" "dbtopow" "dbtorms" "mod" "div" "sin" "cos" "tan" "atan" "atan2" "sqrt" "log" "exp" "abs" "random" "max" "min" "clip" "wrap" ; math
                 "notein" "ctlin" "pgmin" "bendin" "touchin" "polytouchin" "midiin" "sysexin" "noteout" "ctlout" "pgmout" "bendout" "touchout" "polytouchout" "midiout" "makenote" "stripnote" "oscparse" "oscformat" ; midi and osc
                 "tabread" "tabread4" "tabwrite" "soundfiler" "table" "array" ; arrays / tables
                 "loadbang" "serial" "netsend" "netreceive" "qlist" "textfile" "openpanel" "savepanel" "bag" "poly" "key" "keyup" "keyname" "declare" ; misc
                 "+~" "-~" "*~" "/~" "max~" "min~" "clip~" "q8_rsqrt~" "q8_sqrt~" "sqrt~" "wrap~" "fft~" "ifft~" "rfft~" "rifft~" "pow~" "log~" "exp~" "abs~" "framp~" "mtof~" "ftom~" "rmstodb~" "dbtorms~" ; audio math
                 "dac~" "adc~" "sig~" "line~" "vline~" "threshold~" "snapshot~" "vsnapshot~" "bang~" "samplerate~" "send~" "receive~" "throw~" "catch~" "block~" "switch~" "readsf~" "writesf~" ; audio manipulation
                 "phasor~" "cos~" "osc~" "tabwrite~" "tabplay~" "tabread~" "tabread4~" "tabosc4~" "tabsend~" "tabreceive~" ; audio oscillators and tables
                 "vcf~" "noise~" "env~" "hip~" "lop~" "bp~" "biquad~" "samphold~" "print~" "rpole~" "rzero~" "rzero_rev~" "cpole~" "czero~" "czero_rev~" ; audio filters
                 "delwrite~" "delread~" "vd~" ; audio delay
                 ;; pd
                 "inlet" "outlet" "inlet~" "outlet~" ; subwindows
                 "struct" "drawcurve" "filledcurve" "drawpolygon" "filledpolygon" "plot" "drawnumber" ; data templates
                 "pointer" "get" "set" "element" "getsize" "setsize" "append" "scalar" ; accessing data
                 "sigmund~" "bonk~" "choice" "hilbert~" "complex-mod~" "expr~" "expr" "fexpr~" "loop~" "lrshift~" "pd~" "rev1~" "rev2~" "rev3~" "bob~"}) ; extras

(def self-nodes #{"msg" "text"})

(def patch-defaults
  ;; default :options for patch maps
  {:type :patch
   :x 0
   :y 0
   :width 450
   :height 300})

(def default-options
  ;; default :options for node maps
  {obj-nodes {:x 0 :y 0}
   self-nodes {:x 0 :y 0}})

;; templates are used to transform clojure maps into the actual strings in a puredata file.
(def node-templates {obj-nodes ["#X" "obj" :x :y :op :args]
                     self-nodes ["#X" :op :x :y :args]})
(def connection-template ["#X" "connect" [:from-node :id] [:from-node :outlet] [:to-node :id] [:to-node :inlet]])
(def patch-header-template ["#N" "canvas" :x :y :width :height 10])
(def patch-footer-template ["#X" "coords" "0 1 100 -1 200 140 1"]) ;; TODO figure out later
(def subpatch-footer-template ["#X" "restore" "128 184" name]) ;; TODO figure out later

(defn- merge-options
  [defaults n]
  (assoc n :options (merge defaults (:options n))))

(defn- to-string
  "Stringify literals conformant to the puredata patch format.
  E.g. formatting floats and rationals accordingly, TODO escaping
  \";\" and \"$\" characters etc."
  [elm]
  (cond
    (coll? elm) (if (empty? elm)
                  nil
                  (string/join " " (map to-string elm)))
    (number? elm) (cond
                    (integer? elm) (str elm)
                    (float? elm) (format "%f" elm)
                    (rational? elm) (to-string (float elm)))
    :else (str elm)))

(defn- fill-template
  "Use a vector T of literals and keys to construct a string representation of the map M."
  [t m]
  (->
   (string/join
    " "
    (remove nil?
            (for [lookup t]
              (cond (string? lookup) lookup
                    (number? lookup) (str lookup)
                    (keyword? lookup) (to-string (or (lookup (:options m)) (lookup m)))
                    (vector? lookup) (to-string (get-in m lookup))))))
   (str ";")))

(defn- translate-node
  "Check for the presence of the :op of node N in the key sets of NODE-TEMPLATES, then use the corresponding template value to construct a string representation of the node."
  [n]
  (loop [[k & rst] (keys node-templates)]
    (cond (nil? k) (throw (Exception. (str "Not a valid node: " n)))
          (k (:op n)) (->> n
                           (merge-options (default-options k))
                           (fill-template (node-templates k)))
          :else (recur rst))))

(defn- translate-any
  "Construct a string representation for any map X using TEMPLATE."
  [template x]
  (fill-template template x))

(defn translate-line
  "Dispatch on the :type of map L to choose a matching template for constructing its string representation."
  [l]
  (condp (fn [te e] (= te (:type e))) l
    :node            (translate-node l)
    :connection      (translate-any connection-template l)
    :patch-header    (translate-any patch-header-template l)
    :patch-footer    (translate-any patch-footer-template l)
    :subpatch-footer (translate-any subpatch-footer-template l)))

(defn wrap-lines
  "While nodes and connections are represented as single lines in a puredata patch file, the patch itself is defined over several lines,enclosing the other content in the form of headers and footers."
  [patch lines]
  (let [{:keys [graph-on-parent subpatch]} patch
        header-keys (select-keys patch [:x :y :width :height])
        footer-keys (-> patch
                        (select-keys [:graph-on-parent :view-width :view-height])
                        (update :graph-on-parent (fn [bool] (if bool 1 0))))
        subpatch-footer-keys (select-keys patch [:subpatch :parent-x :parent-y :name])
        header (merge {:type :patch-header}
                      header-keys)
        footer-or-nil (and graph-on-parent
                           (merge {:type :patch-footer}
                                  footer-keys))
        subpatch-footer-or-nil (and subpatch
                                    (merge {:type :subpatch-footer}
                                           subpatch-footer-keys))]
    (remove nil? (cons header (conj lines footer-or-nil subpatch-footer-or-nil)))))

(defn write-patch
  [file lines]
  (spit file (string/join "\n" lines)))

(defmacro with-patch
  [name options & rest]
  (let [[patch forms] (if (map? options)
                        [(merge patch-defaults options) rest]
                        [patch-defaults (cons options rest)])]
    `(let [lines# (wrap-lines ~patch (:lines (in-context ~@forms)))
           out# (map translate-line lines#)]
       (write-patch ~name out#)
       (reload))))
