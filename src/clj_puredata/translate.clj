(ns clj-puredata.translate
  "Translate the nodes and connections of a patch into into their PureData-conformant string representation.
  Includes a set of templates (vectors of keywords (and literals)),
  which are used to construct the string by picking corresponding
  values from clojure maps. Also includes default values for various
  types to make sure all necessary keywords are present for filling
  the templates."
  (:require [clojure.string :as string]
            [clj-puredata.parse :refer [lines]]
            [clj-puredata.comms :refer [reload]]
            [clj-puredata.common :refer :all]))

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

(def atom-nodes
  #{"floatatom" "symbolatom"})

(def floatatom-node
  #{"floatatom"})

(def symbolatom-node
  #{"symbolatom"})

(def bng-node
  #{"bng"})

(def tgl-node
  #{"tgl"})

(def slider-nodes
  #{"hsl" "vsl"})

(def common-node-defaults
  "Super ultra lowest common denominator."
  {:x 0 :y 0})

(def atom-node-defaults
  "Common defaults for floatatom and symbolatom nodes."
  (merge common-node-defaults
         {:send-symbol "-"
          :receive-symbol "-"
          :label-text "-"
          :label-pos 0
          :width 5
          :lower-limit 0
          :upper-limit 0}))

(def ui-node-defaults
  "Common defaults for bng, tgl, cnv and slider nodes."
  (merge common-node-defaults
         {:send-symbol "empty"
          :receive-symbol "empty"
          :label-text "empty"
          :label-x 17
          :label-y 7
          :font-family 0
          :font-size 10
          :bg-color -262144
          :fg-color -1
          :label-color -1
          :size 15}))

(def slider-node-defaults
  (merge ui-node-defaults
         {:width 15
          :height 15 ; same for horizontal and vertical because lazy
          :bottom 0
          :top 127
          :log 0
          :init 0
          :default 0 ; default means: saved slider position, in pixels.
          :steady-on-click 1}))

(def node-defaults
  "Default :options for node maps, used to fill node templates"
  {obj-nodes common-node-defaults
   self-nodes common-node-defaults
   floatatom-node (merge atom-node-defaults {:width 5})
   symbolatom-node (merge atom-node-defaults {:width 10})
   bng-node (merge ui-node-defaults {:hold 250 :interrupt 50 :init 0})
   tgl-node (merge ui-node-defaults {:init 0 :init-value 0  :nonzero-value 1})
   slider-nodes slider-node-defaults})

(def node-templates
  "Correlate sets of nodes with common templates."
  {obj-nodes ["#X" "obj" :x :y :op :args]
   self-nodes ["#X" :op :x :y :args]
   atom-nodes ["#X" :op :x :y :width :lower-limit :upper-limit :label-pos :label-text :receive-symbol :send-symbol]
   bng-node ["#X obj" :x :y :op :size :hold :interrupt :init :send-symbol :receive-symbol :label-text :label-x :label-y :font-family :font-size :bg-color :fg-color :label-color]
   tgl-node ["#X obj" :x :y :op :size :init :send-symbol :receive-symbol :label-text :label-x :label-y :font-family :font-size :bg-color :fg-color :label-color :init-value :nonzero-value]
   slider-nodes ["#X obj" :x :y :op :width :height :bottom :top :log :init :send-symbol :receive-symbol :label-text :label-x :label-y :font-family :font-size :bg-color :fg-color :label-color :default :steady-on-click]})

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
    (string? elm) (-> elm
                      (string/replace #"\$" #(str \\ %))
                      (string/replace #"[,;]" #(str " \\" %)))
    :else (to-string (str elm))))

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



(defn- op-suggests-patch?
  "True for nodes whose :op string ends in \".pd\"."
  [n]
  (string/ends-with? (:op n) ".pd"))

(defn- strip-file-extension
  "Remove trailing \".pd\" from :op string of node."
  [n]
  (assoc n :op (string/replace (:op n) #"\.pd$" "")))

(defn- get-by-matching-key-set
  "Find MATCH in the key sets of map M; return the correlated value in M.
  Assumes that the keys of M are clojure sets."
  [m match]
  (loop [[k & ks] (keys m)]
    (cond (nil? k) (throw (Exception. (str "No key found matching " match " in: " m)))
          (k match) (m k)
          :else (recur ks))))

(defn- get-node-template
  [n]
  (get-by-matching-key-set node-templates (:op n)))

(defn- get-node-default
  [n]
  (get-by-matching-key-set node-defaults (:op n)))

(defn translate-node
  "Match node to template, merge with default options and return filled template.
  Checks for the presence of the :op-string of node N in the key sets
  of NODE-TEMPLATES, then uses the corresponding template value to
  construct a string representation of the node. Special case
  where :op ends with \".pd\", which references another patch, and
  uses the same template as other 'obj'-nodes."
  [n]
  (if (op-suggests-patch? n)
    (->> (strip-file-extension n)
         (merge-options (node-defaults obj-nodes))
         (fill-template (node-templates obj-nodes)))
    (->> n
         (merge-options (get-node-default n))
         (fill-template (get-node-template n)))))

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

(defn with-patch
  [name options & rest]
  (assert (string? name))
  (let [[patch forms] (if (and (map? options)
                               (not (node? options)))
                        [(merge patch-defaults options) rest]
                        [patch-defaults (cons options rest)])]
    (let [lines (->> (lines forms)
                     (wrap-lines patch)
                     (move-layout patch))
          out (map translate-line lines)]
      ;;(write-patch name out)
      ;;(reload)
      out
      )))
