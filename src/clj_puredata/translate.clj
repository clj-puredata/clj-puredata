(ns clj-puredata.translate
  (:require [clojure.string :as string]))

(def obj-nodes #{;; -------- general --------
                 "bang" "b" "float" "f" "symbol" "int" "i" "send" "s" "receive" "r" "select" "route" "pack" "unpack" "trigger" "t" "spigot" "moses" "until" "print" "makefilename" "change" "swap" "value" "v" "list"
                 ;; -------- time --------
                 "delay" "metro" "line" "timer" "cputime" "realtime" "pipe"
                 ;; -------- math --------
                 "+" "-" "*" "/" "pow" "==" "!=" ">" "<" ">=" "<=" "&" "&&" "||" "||||" "%" "<<" ">>" "mtof" "powtodb" "rmstodb" "ftom" "dbtopow" "dbtorms" "mod" "div" "sin" "cos" "tan" "atan" "atan2" "sqrt" "log" "exp" "abs" "random" "max" "min" "clip" "wrap"
                 ;; -------- midi and osc --------
                 "notein" "ctlin" "pgmin" "bendin" "touchin" "polytouchin" "midiin" "sysexin" "noteout" "ctlout" "pgmout" "bendout" "touchout" "polytouchout" "midiout" "makenote" "stripnote" "oscparse" "oscformat"
                 ;; -------- arrays / tables --------
                 "tabread" "tabread4" "tabwrite" "soundfiler" "table" "array"
                 ;; -------- misc --------
                 "loadbang" "serial" "netsend" "netreceive" "qlist" "textfile" "openpanel" "savepanel" "bag" "poly" "key" "keyup" "keyname" "declare"
                 ;; -------- audio math --------
                 "+~" "-~" "*~" "/~" "max~" "min~" "clip~" "q8_rsqrt~" "q8_sqrt~" "sqrt~" "wrap~" "fft~" "ifft~" "rfft~" "rifft~" "pow~" "log~" "exp~" "abs~" "framp~" "mtof~" "ftom~" "rmstodb~" "dbtorms~"
                 ;; -------- general audio manipulation --------
                 "dac~" "adc~" "sig~" "line~" "vline~" "threshold~" "snapshot~" "vsnapshot~" "bang~" "samplerate~" "send~" "receive~" "throw~" "catch~" "block~" "switch~" "readsf~" "writesf~"
                 ;; -------- audio oscillators and tables --------
                 "phasor~" "cos~" "osc~" "tabwrite~" "tabplay~" "tabread~" "tabread4~" "tabosc4~" "tabsend~" "tabreceive~"
                 ;; -------- audio filters --------
                 "vcf~" "noise~" "env~" "hip~" "lop~" "bp~" "biquad~" "samphold~" "print~" "rpole~" "rzero~" "rzero_rev~" "cpole~" "czero~" "czero_rev~"
                 ;; -------- audio delay --------
                 "delwrite~" "delread~" "vd~"
                 ;; -------- subwindows --------
                 ;; pd
                 "inlet" "outlet" "inlet~" "outlet~"
                 ;; -------- data templates --------
                 "struct" "drawcurve" "filledcurve" "drawpolygon" "filledpolygon" "plot" "drawnumber"
                 ;; -------- accessing data --------
                 "pointer" "get" "set" "element" "getsize" "setsize" "append" "scalar"
                 ;; -------- extras --------
                 "sigmund~" "bonk~" "choice" "hilbert~" "complex-mod~" "expr~" "expr" "fexpr~" "loop~" "lrshift~" "pd~" "rev1~" "rev2~" "rev3~" "bob~"})

(def self-nodes #{"msg" "text"})

(def default-options
  {obj-nodes {:x 0 :y 0}
   self-nodes {:x 0 :y 0}})

(def node-templates
  {obj-nodes ["#X" "obj" :x :y :op :args]
   self-nodes ["#X" :op :x :y :args]})

(def connection-template ["#X" "connect" [:from-node :id] [:from-node :outlet] [:to-node :id] [:to-node :inlet]])
(def patch-header-template ["#N" "canvas" :x :y :width :height 10])
(def patch-footer-template ["#X" "coords" "0 1 100 -1 200 140 1"]) ;; TODO figure out later
(def subpatch-footer-template ["#X" "restore" "128 184" name]) ;; TODO figure out later

(defn merge-options [defaults n]
  (assoc n :options (merge defaults (:options n))))

(defn- to-string [elm]
  (cond
    (coll? elm) (if (empty? elm)
                  nil
                  (string/join " " (map to-string elm)))
    (number? elm) (cond
                    (integer? elm) (str elm)
                    (float? elm) (format "%f" elm)
                    (rational? elm) (to-string (float elm)))
    :else (str elm)))

(defn fill-template [t n]
  (->
   (string/join
    " "
    (remove nil?
            (for [lookup t]
              (cond (string? lookup) lookup
                    (number? lookup) (str lookup)
                    (keyword? lookup) (to-string (or (lookup (:options n)) (lookup n)))
                    (vector? lookup) (to-string (get-in n lookup))))))
   (str ";")))

(defn translate-node [n]
  (loop [[k & rst] (keys node-templates)]
    (cond (nil? k) (throw (Exception. (str "Not a valid node: " n)))
          (k (:op n)) (->> n
                           (merge-options (default-options k))
                           (fill-template (node-templates k)))
          :else (recur rst))))

(defn translate-any [template x] (fill-template template x))

(defn translate-line [l]
  (condp (fn [te e] (= te (:type e))) l
    :node (translate-node l)
    :connection (translate-any connection-template l)
    :patch-header (translate-any patch-header-template l)
    :patch-footer (translate-any patch-footer-template l)
    :subpatch-footer (translate-any subpatch-footer-template l)))
