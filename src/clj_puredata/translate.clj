(ns clj-puredata.translate
  "Translate the nodes and connections of a patch into into their PureData-conformant string representation.
  Includes a set of templates (vectors of keywords (and literals)),
  which are used to construct the string by picking corresponding
  values from clojure maps. Also includes default values for various
  types to make sure all necessary keywords are present for filling
  the templates."
  (:require [clojure.string :as string]
            [clj-puredata.parse :refer [in-context]]
            [clj-puredata.puredata :refer [reload]]))

(def obj-nodes
  "Set of default node types ('obj') available in PureData 0.47 (Vanilla)."
  #{"bang" "b" "float" "f" "symbol" "int" "i" "send" "s" "receive" "r" "select" "route" "pack" "unpack" "trigger" "t" "spigot" "moses" "until" "print" "makefilename" "change" "swap" "value" "v" "list" ; general
    "delay" "metro" "line" "timer" "cputime" "realtime" "pipe" ; time
    "+" "-" "*" "/" "pow" "==" "!=" ">" "<" ">=" "<=" "&" "&&" "||" "||||" "%" "<<" ">>" "mtof" "powtodb" "rmstodb" "ftom" "dbtopow" "dbtorms" "mod" "div" "sin" "cos" "tan" "atan" "atan2" "sqrt" "log" "exp" "abs" "random" "max" "min" "clip" "wrap" ; math
    "notein" "ctlin" "pgmin" "bendin" "touchin" "polytouchin" "midiin" "sysexin" "noteout" "ctlout" "pgmout" "bendout" "touchout" "polytouchout" "midiout" "makenote" "stripnote" "oscparse" "oscformat" ; midi and osc
    "tabread" "tabread4" "tabwrite" "soundfiler" "table" "array" ; arrays / tables
    "loadbang" "serial" "netsend" "netreceive" "qlist" "textfile" "openpanel" "savepanel" "bag" "poly" "key" "keyup" "keyname" "declare" ; misc
    "+~" "-~" "*~" "/~" "max~" "min~" "clip~" "q8_rsqrt~" "q8_sqrt~" "sqrt~" "wrap~" "fft~" "ifft~" "rfft~" "rifft~" "pow~" "log~" "exp~" "abs~" "framp~" "mtof~" "ftom~" "rmstodb~" "dbtorms~" ; audio math
    "dac~" "adc~" "sig~" "line~" "vline~" "threshold~" "snapshot~" "vsnapshot~" "bang~" "samplerate~" "send~" "receive~" "throw~" "catch~" "block~" "switch~" "readsf~" "writesf~" ; audio manipulation
    "phasor~" "cos~" "osc~" "tabwrite~" "tabplay~" "tabread~" "tabread4~" "tabosc4~" "tabsend~" "tabreceive~" ; audio oscillators and tables
    "vcf~" "noise~" "env~" "hip~" "lop~" "bp~" "biquad~" "samphold~" "print~" "rpole~" "rzero~" "rzero_rev~" "cpole~" "czero~" "czero_rev~" ; audio filters
    "delwrite~" "delread~" "vd~"                     ; audio delay
    ;; pd
    "inlet" "outlet" "inlet~" "outlet~" ; subwindows
    "struct" "drawcurve" "filledcurve" "drawpolygon" "filledpolygon" "plot" "drawnumber" ; data templates
    "pointer" "get" "set" "element" "getsize" "setsize" "append" "scalar" ; accessing data
    "sigmund~" "bonk~" "choice" "hilbert~" "complex-mod~" "expr~" "expr" "fexpr~" "loop~" "lrshift~" "pd~" "rev1~" "rev2~" "rev3~" "bob~"}) ; extras

(def self-nodes
  "Set of nodes with simple representations using their name."
  #{"msg" "text"})

(def node-defaults
  "Default :options for node maps, used to fill node templates"
  {obj-nodes {:x 0 :y 0}
   self-nodes {:x 0 :y 0}})

(def node-templates
  "Correlate sets of nodes with common templates."
  {obj-nodes ["#X" "obj" :x :y :op :args]
   self-nodes ["#X" :op :x :y :args]})

(def connection-template ["#X" "connect"
                          [:from-node :id]
                          [:from-node :outlet]
                          [:to-node :id]
                          [:to-node :inlet]])

(def patch-defaults
  "Default :options for patch maps, used to fill the patch templates."
  {:type :patch
   ;; window properties
   :x 0
   :y 0
   :width 450
   :height 300
   ;; graph properties
   :graph-on-parent false
   :hide-object-name false
   :view-width 85
   :view-height 60
   :view-margin-x 0
   :view-margin-y 0
   :x-range-min 0
   :x-range-max 100
   :y-range-min 1
   :y-range-max -1})

(def patch-header-template ["#N" "canvas"
                            :x :y :width :height
                            10])

(def patch-footer-template ["#X" "coords"
                            :x-range-min :y-range-min
                            :x-range-max :y-range-max
                            :view-width :view-height
                            :graph-on-parent
                            :view-margin-x :view-margin-y])

#_(def subpatch-header-template ["#N" "canvas" :x :y :width :height :subpatch-name :visible])
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
                           (merge-options (node-defaults k))
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

(defn numberize-graph-on-parent
  "Conflate :graph-on-parent and :hide-object-name into a single number.
  The option :graph-on-parent is conflated with the option :hide-object-name when represented in the patch file.
  0 means :graph-on-parent is false, 1 means :graph-on-parent is true but :hide-object-name is false, 2 means both are true.
  The fourth case does not matter and is not represented."
  [m]
  (assoc m :graph-on-parent (cond (and (:graph-on-parent m) (:hide-object-name m)) 2
                                  (:graph-on-parent m) 1
                                  :else 0)))

(defn wrap-lines
  "Enclose nodes and connections with patch footer and header lines.
  While nodes and connections are represented as single lines in a
  puredata patch file, the patch itself is defined over several lines,
  enclosing the other content in the form of headers and footers.
  This function takes care of creating any that apply."
  [patch lines]
  (let [{:keys [graph-on-parent subpatch]} patch
        ;;
        header-keys (select-keys patch [:x :y :width :height])
        footer-keys (-> patch
                        (select-keys [:graph-on-parent :hide-object-name
                                      :x-range-min :y-range-min
                                      :x-range-max :y-range-max
                                      :view-width :view-height
                                      :view-margin-x :view-margin-y])
                        numberize-graph-on-parent)
        subpatch-footer-keys (select-keys patch [:subpatch :parent-x :parent-y :name])
        ;;
        header (merge {:type :patch-header}
                      header-keys)
        footer-or-nil (if graph-on-parent (merge {:type :patch-footer} footer-keys)
                          nil)
        subpatch-footer-or-nil (if subpatch (merge {:type :subpatch-footer} subpatch-footer-keys)
                                   nil)]
    (remove nil? (cons header (conj lines footer-or-nil subpatch-footer-or-nil)))))

(defn move-layout
  "Move auto-layouted nodes out of graph-on-parent region.
  That region is used for deliberately placed interface elements."
  [patch lines]
  (if (:graph-on-parent patch)
    (mapv (fn [l]
            (if (:auto-layout l)
              (update-in l [:options :y] (fn [y] (+ y (+ (:view-height patch) (:view-margin-y patch)))))
              l))
          lines)
    lines))

(defn write-patch
  [file lines]
  (spit file (string/join "\n" lines)))

(defmacro with-patch
  [name options & rest]
  (let [[patch forms] (if (map? options)
                        [(merge patch-defaults options) rest]
                        [patch-defaults (cons options rest)])]
    `(let [lines# (->> (in-context ~@forms)
                       :lines
                       (wrap-lines ~patch)
                       (move-layout ~patch))
           out# (map translate-line lines#)]
       (write-patch ~name out#)
       (reload))))
