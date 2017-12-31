(ns clj-puredata.parse
  (:require [clojure.test :as t]
            [vijual :as v]))

(def parse-context (atom nil))

(defn- hiccup?
  [form]
  (and (vector? form)
       (or (keyword? (first form))
           (string? (first form)))))

(defn- literal?
  "Returns TRUE for numbers, strings and NIL."
  [arg]
  (if (or (number? arg)
          (string? arg)
          (char? arg)
          (nil? arg))
    true
    false))

(defn- node?
  [arg]
  (and (map? arg)
       (= (:type arg) :node)))

(defn- connection?
  [arg]
  (and (map? arg)
       (= (:type arg) :connection)))

(defn- other?
  "See OTHER."
  [node]
  (and (nil? (:id node))
       (some? (:other node))))

(defn- processed?
  [node]
  ((:processed-node-ids @parse-context) (:id node)))

(defn- record-as-processed
  [node]
  (swap! parse-context update :processed-node-ids conj (:id node)))

(defn- node-or-explicit-skip?
  "When determining the target inlet of a connection, the source nodes argument position is consulted.
  An argument of NIL is interpreted as explicitly 'skipping' an inlet.
  Any other arguments (literals/numbers/strings) are ignored in this count."
  [x]
  (or (node? x) (nil? x)))

(defn setup-parse-context []
  (reset! parse-context {:current-node-id 0
                         :lines []
                         :processed-node-ids #{}}))

(defn teardown-parse-context []
  (reset! parse-context nil))

(defn- add-element!
  "Add NODE to the current PARSE-CONTEXT."
  [e]
  (swap! parse-context update :lines conj e)
  e)

(defn- dispense-node-id
  "When a PARSE-CONTEXT is active, dispense one new (running) index."
  []
  (if-let [id (:current-node-id @parse-context)]
    (do (swap! parse-context update :current-node-id inc)
        id)
    -1))

(defn- resolve-other
  "Try to find the referenced node in the current PARSE-CONTEXT."
  [other]
  (let [solve (first (filter #(= (:other other) (get-in % [:options :name]))
                             (@parse-context :lines)))]
    (if (nil? solve)
      (throw (Exception. (str "Cannot resolve other node " other)))
      solve)))

(defn resolve-all-other!
  "Resolve references to OTHER nodes in connections with the actual node ids.
  Called by IN-CONTEXT once all nodes have been walked."
  []
  (swap! parse-context update :lines
         (fn [lines]
           (vec (for [l lines]
                  (cond
                    (node? l) l
                    (connection? l) (let [from (get-in l [:from-node :id])]
                                      (if (other? from)
                                        (assoc-in l [:from-node :id] (:id (resolve-other from)))
                                        l))
                    :else l))))))

#_(defn- resolve
    "Resolve anything to a node, wether it already is or not."
    [n]
    (if (other? n)
      (resolve-other n)
      n))

(defn- assoc-layout
  [layout line]
  (if (node? line)
    (let [fac 60
          pos (first (filter #(= (str (:id line)) (:text %)) layout))]
      (if pos
        (-> line
            (assoc-in [:options :x] (* fac (:x pos)))
            (assoc-in [:options :y] (* fac (:y pos))))
        line))
    line))

(defn layout-lines
  [lines]
  (let [cs (filter #(= :connection (:type %)) lines)
        es (map #(vector (get-in % [:from-node :id])
                         (get-in % [:to-node :id]))
                cs)]
    (if (empty? es)
      lines
      (mapv (partial assoc-layout (v/layout-graph v/ascii-dim es {} true))
            lines))))

(defn sort-lines
  [lines]
  (->> lines
       (sort-by :id)
       (sort-by (comp :id :from-node))
       vec))

(defn- subs-trailing-dash
  "In strings > 1 character containing a trailing dash \"-\", substitute a tilde \"~\"."
  [op]
  (clojure.string/replace op #"^(.+)-$" "$1~"))

(defn- op-from-kw
  "Keyword -> string, e.g. :+ -> \"+\".
  Turns keywords containing trailing dashes into strings with trailing tildes, e.g. :osc- -> \"osc~\".
  Recognizes & passes strings untouched."
  [op-kw]
  (if (keyword? op-kw)
    (subs-trailing-dash (name op-kw))
    (str op-kw)))

(defn- remove-node-args
  [node]
  (update node :args (comp vec (partial remove node-or-explicit-skip?))))

(defn- connection
  [from-node to-id inlet]
  {:type :connection
   :from-node {:id (if (other? from-node)
                     from-node
                     (:id from-node))
               :outlet (:outlet from-node 0)}
   :to-node {:id to-id
             :inlet (:inlet from-node inlet)}})

(declare walk-tree!)

(defn- walk-node-args
  [node]
  (let [connected-nodes (filter node-or-explicit-skip? (:args node))]
       (when (not (empty? connected-nodes))
         (doall (map-indexed (fn [i c] (when (node? c) (walk-tree! c (:id node) i)))
                             connected-nodes)))))

(defn walk-tree!
  "The main, recursive function responsible for adding nodes and connections to the PARSE-CONTEXT.
  Respects special cases for OTHER, INLET and OUTLET nodes."
  ([node parent-id inlet]
   (add-element! (connection node parent-id inlet))
   (when-not (other? node) (walk-tree! node)))
  ([node]
   (when (not (processed? node))
     (record-as-processed node)
     (add-element! (remove-node-args node))
     (walk-node-args node))))

(defmacro in-context
  "Set up fresh PARSE-CONTEXT, evaluate patch forms, return lines ready for translation."
  [& forms]
  `(do
     (setup-parse-context)
     (let [nodes# (vector ~@forms)]
       (doall (map walk-tree! nodes#))
       (resolve-all-other!)
       (let [lines# (layout-lines (sort-lines (:lines @parse-context)))]
         (teardown-parse-context)
         {:nodes nodes#
          :lines lines#}))))

(defn pd
  "Turn hiccup vectors into trees of node maps, ready to be walked by WALK-TREE!."
  [form]
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
    (literal? form) form
    (node? form) form
    (fn? form) (form)
    :else (throw (Exception. (str "Not any recognizable form: " form)))))

(defn outlet
  "Use OUTLET to specify the intended outlet of a connection, e.g. (pd [:+ (outlet (pd [:moses ...]) 1)]). The default outlet is 0."
  [node n]
  (assoc node :outlet n))

(defn inlet
  "Use INLET to specify the intended inlet for a connection, e.g. (pd [:/ 1 (inlet (pd ...) 1)]). The default inlet is determined by the source node argument position (not counting literals, only NIL and other nodes) (e.g. 0 in the previous example)."
  [node n]
  (assoc node :inlet n))

(defn other
  "An OTHER is a special node that refers to a previously defined node with :name = NAME in its :options map. It can be used to reduce the number of LETs in patch definitions, e.g. (pd [:osc- {:name \"foo\"} 200]) (pd [:dac- (other \"foo\") (other \"foo\")])."
  [name]
  {:type :node
   :other name})

#_(defn connect
    "Connect two nodes.
  This verb is necessary because argument or OTHER nodes can only be
  previously defined nodes. Since nodes cannot be re-defined, this is
  the appropriate way to treat nodes that are mutually connected."
    ;; problem: how to pass INLET and OUTLET through OTHER?
    [to & froms]
    (doall
     (map-indexed (fn [i c]
                    (add-element! (connection c (:id to) (or (:inlet to) i))))
                  froms)))
