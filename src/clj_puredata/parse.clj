(ns clj-puredata.parse
  (:require [clojure.test :as t]
            [vijual :as v]))

(def parse-context (atom nil))

(defn- hiccup? [form]
  (and (vector? form)
       (or (keyword? (first form))
           (string? (first form)))))

(defn- literal? [arg]
  "Returns TRUE for numbers, strings and NIL."
  (if (or (number? arg) 
          (string? arg)
          (char? arg)
          (nil? arg))
    true
    false))

(defn- node? [arg]
  (and (map? arg)
       (= (:type arg) :node)))

(defn- other? [node]
  "See OTHER."
  (and (nil? (:id node))
       (some? (:other node))))

(defn- processed? [node]
  ((:processed-node-ids @parse-context) (:id node)))

(defn- node-or-explicit-skip? [x]
  "When determining the target inlet of a connection, the source nodes argument position is consulted. An argument of NIL is interpreted as explicitly 'skipping' an inlet. Any other arguments (literals/numbers/strings) are ignored in this count."
  (or (node? x) (nil? x)))

(defn setup-parse-context []
  (reset! parse-context {:current-node-id 0
                         :lines []
                         :processed-node-ids #{}}))

(defn teardown-parse-context []
  (reset! parse-context nil))

(defn- add-element [e]
  "Add NODE to the current PARSE-CONTEXT."
  (swap! parse-context update :lines conj e)
  e)

(defn- dispense-node-id []
  "When a PARSE-CONTEXT is active, dispense one new (running) index."
  (if-let [id (:current-node-id @parse-context)]
    (do (swap! parse-context update :current-node-id inc)
        id)
    -1))

(defn- resolve-other [other]
  "Try to find the referenced node in the current PARSE-CONTEXT."
  (let [solve (first (filter #(= (:other other) (get-in % [:options :name]))
                             (@parse-context :lines)))]
    (if (nil? solve)
      (throw (Exception. (str "Cannot resolve other node " other)))
      solve)))

(defn- assoc-layout [layout line]
  (if (node? line)
    (let [fac 60
          pos (first (filter #(= (str (:id line)) (:text %)) layout))]
      (if pos
        (-> line
            (assoc-in [:options :x] (* fac (:x pos)))
            (assoc-in [:options :y] (* fac (:y pos))))
        line))
    line))

(defn layout-lines [lines]
  (let [cs (filter #(= :connection (:type %)) lines)
        es (map #(vector (get-in % [:from-node :id])
                         (get-in % [:to-node :id]))
                cs)]
    (if (empty? es)
      lines
      (mapv (partial assoc-layout (v/layout-graph v/ascii-dim es {} true))
            lines))))

(defn sort-lines [lines]
  (->> lines
       (sort-by :id)
       (sort-by (comp :id :from-node))
       vec))

(defn- subs-trailing-dash [op]
  "In strings > 1 character containing a trailing dash \"-\", substitute a tilde \"~\"."
  (clojure.string/replace op #"^(.+)-$" "$1~"))

(defn- op-from-kw [op-kw]
  "Keyword -> string, e.g. :+ -> \"+\". Turns keywords containing trailing dashes into strings with trailing tildes, e.g. :osc- -> \"osc~\". Recognizes & passes strings untouched."
  (if (keyword? op-kw)
    (subs-trailing-dash (name op-kw))
    (str op-kw)))

(defn walk-tree!
  "The main, recursive function responsible for adding nodes and connections to the PARSE-CONTEXT. Respects special cases for OTHER, INLET and OUTLET nodes."
  ([node parent inlet]
   ;; add a connection, then recur.
   (add-element {:type :connection
                 :from-node {:id (if (other? node)
                                   (:id (resolve-other node))
                                   (:id node))
                             :outlet (:outlet node 0)}
                 :to-node {:id parent
                           :inlet (:inlet node inlet)}})
   (when-not (other? node) (walk-tree! node)))
  ([node]
   ;; process node, then recur on any arguments of type node (to make connections).
   (when (not (processed? node))
     (swap! parse-context update :processed-node-ids conj (:id node))
     (add-element (update node :args (comp vec (partial remove node?))))
     (let [connected-nodes (filter node-or-explicit-skip? (:args node))]
       (when (not (empty? connected-nodes))
         (doall (map-indexed (fn [i c] (when (node? c) (walk-tree! c (:id node) i)))
                             connected-nodes)))))))

(defmacro in-context [& forms]
  "Set up fresh PARSE-CONTEXT, evaluate patch forms, return lines ready for translation."
  `(do
     (setup-parse-context)
     (let [nodes# (vector ~@forms)]
       (doall (map walk-tree! nodes#))
       (let [lines# (layout-lines (sort-lines (:lines @parse-context)))]
         (teardown-parse-context)
         {:nodes nodes#
          :lines lines#}))))

(defn pd [form]
  ""
  (cond
    (hiccup? form)
    (let [[options args] (if (and (map? (second form))
                                  (not (node? (second form))))
                           [(second form) (drop 2 form)]
                           [{} (rest form)])
          op (op-from-kw (first form))
          id (dispense-node-id)
          parsed-args (mapv pd args)
          node {:type :node :op op :id id :options options :args parsed-args}]
      node)
    ;;
    (literal? form) form
    (node? form) form
    (fn? form) (form)
    :else (throw (Exception. (str "Not any recognizable form: " form)))))
(defn outlet [node n]
  "Use OUTLET to specify the intended outlet of a connection, e.g. (pd [:+ (outlet (pd [:moses ...]) 1)]). The default outlet is 0."
  (assoc node :outlet n))

(defn inlet [node n]
  "Use INLET to specify the intended inlet for a connection, e.g. (pd [:/ 1 (inlet (pd ...) 1)]). The default inlet is determined by the source node argument position (not counting literals, only NIL and other nodes) (e.g. 0 in the previous example)."
  (assoc node :inlet n))

(defn other [name]
  "An OTHER is a special node that refers to a previously defined node with :name = NAME in its :options map. It can be used to reduce the number of LETs in patch definitions, e.g. (pd [:osc- {:name \"foo\"} 200]) (pd [:dac- (other \"foo\") (other \"foo\")])."
  {:type :node
   :other name})

