(ns clj-puredata.parse
  "Facilites for parsing hiccup-style PureData node definitions into Clojure maps, and automatically generating connection entities as needed."
  (:require [clojure.test :as t]
            [clj-puredata.common :refer :all]
            [clj-puredata.layout :as l]))

(defonce counter (atom 0))

(def parse-context
  (atom []))

(defn current-context
  "Return last index found in `parse-context`."
  []
  (dec (count @parse-context)))

(defn update-in-parse-context
  "Update the most recent context in `parse-context`."
  [key & rest]
  (apply swap! parse-context update-in [(current-context) key] rest))

(defn- processed?
  "Check if the node is part of the set `:processed-node-ids`."
  [node]
  ((:processed-node-ids (last @parse-context)) (:unique-id node)))

(defn- get-processed-id
  [node]
  (assoc node :id (processed? node)))

(defn- record-as-processed
  "Remember the NODE :id as processed inside the current context."
  [node]
  ;;(swap! parse-context update-in [(current-context) :processed-node-ids] conj (:id node))
  (update-in-parse-context :processed-node-ids assoc (:unique-id node) (:id node)))

(defn- node-or-explicit-skip?
  "When determining the target inlet of a connection, the source nodes argument position is consulted.
  An argument of NIL is interpreted as explicitly 'skipping' an inlet.
  Any other arguments (literals/numbers/strings) are ignored in this count."
  [x]
  (or (node? x) (nil? x)))

(defn setup-parse-context []
  (swap! parse-context conj {:current-node-id 0
                             :lines []
                             :processed-node-ids {}}))

(defn teardown-parse-context []
  (try
    (swap! parse-context pop)
    (catch IllegalStateException e
      [])))

(defn- add-element!
  "Add NODE to the current PARSE-CONTEXT."
  [e]
  (update-in-parse-context :lines conj e)
  e)

(defn- dispense-unique-id
  []
  (swap! counter inc))

(defn- dispense-node-id
  "When a PARSE-CONTEXT is active, dispense one new (running) index."
  ([]
   (if-let [id (:current-node-id (last @parse-context))]
     (do (update-in-parse-context :current-node-id inc)
         id)
     -1))
  ([node]
   (if (other? node)
     node
     (merge node {:id (dispense-node-id)}))))

(defn- resolve-other
  "Try to find the referenced node in the current PARSE-CONTEXT."
  [other]
  (let [solve (first (filter #(= (:other other) (get-in % [:options :name]))
                             ((last @parse-context) :lines)))]
    (if (nil? solve)
      (throw (Exception. (str "Cannot resolve other node " other)))
      solve)))

(defn resolve-all-other!
  "Resolve references to OTHER nodes in connections with the actual node ids.
  Called by IN-CONTEXT once all nodes have been walked."
  []
  (update-in-parse-context :lines
                           (fn [lines]
                             (vec (for [l lines]
                                    (cond
                                      (node? l) l
                                      (connection? l) (let [from (get-in l [:from-node :id])]
                                                        (if (other? from)
                                                          (assoc-in l [:from-node :id] (:id (resolve-other from)))
                                                          l))
                                      :else l))))))

#_(defn- assoc-layout
  [layout line]
  (if (node? line)
    (let [pos (first (filter #(= (str (:id line)) (:text %)) layout))]
      (if (and pos
               (nil? (get-in line [:options :x]))
               (nil? (get-in line [:options :y])))
        (-> line
            (assoc-in [:options :x] (+ 5 (:xpos pos)))
            (assoc-in [:options :y] (+ 5 (:ypos pos)))
            (assoc :auto-layout true))
        line))
    line))

#_(defn layout-lines
  [lines]
  (let [connections (filter #(= :connection (:type %)) lines)
        edges (map #(vector (get-in % [:from-node :id])
                            (get-in % [:to-node :id]))
                   connections)]
    (if (empty? edges)
      lines
      (mapv (partial assoc-layout (v/layout-graph v/image-dim edges {} true))
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

(declare walk-node!)
(defn- walk-node-args!
  [node]
  (let [connected-nodes (filter node-or-explicit-skip? (:args node))]
    (when (not (empty? connected-nodes))
      (doall (map-indexed (fn [i c] (when (node? c) (add-element! (connection (walk-node! c) (:id node) i))))
                          connected-nodes)))))

(defn walk-node!
  "The main, recursive function responsible for adding nodes and connections to the PARSE-CONTEXT.
  Respects special cases for OTHER, INLET and OUTLET nodes."
  ([node]
   (cond
     (other? node) node
     (processed? node) (get-processed-id node)
     :else (let [id-node (dispense-node-id node)]
             (record-as-processed id-node)
             (add-element! (remove-node-args id-node))
             (walk-node-args! id-node)
             id-node))))

(defn in-context
  "Set up fresh PARSE-CONTEXT, evaluate patch forms, return lines ready for translation."
  [& forms]
  (do
    (setup-parse-context)
    (let [nodes (apply vector forms)]
      (doall (map walk-node! (flatten nodes)))
      (resolve-all-other!)
      (let [lines (-> (last @parse-context)
                      :lines
                      l/layout-lines
                      sort-lines)]
        (teardown-parse-context)
        {:nodes nodes
         :lines lines}))))

(defn pd-single
  "Turn hiccup vectors into trees of node maps, ready to be walked by WALK-TREE!."
  [form]
  (cond
    (hiccup? form)
    (let [[options args] (if (and (map? (second form))
                                  (not (node? (second form))))
                           [(second form) (drop 2 form)]
                           [{} (rest form)])
          op (op-from-kw (first form))
          unique-id (dispense-unique-id) ;; FIXME: need to use unique id for determining if node was processed (user might bind a node and reuse it), but current implementation only assigns ids by walking the composed tree (not on first creation). this means that reuse of nodes requires use of `other`. (pd 3/7/2021)
          parsed-args (mapv pd-single args)
          node {:type :node :op op :unique-id unique-id :options options :args parsed-args}]
      node)
    (literal? form) form
    (node? form) form
    (fn? form) (form)
    (or (list? form)
        (vector? form)
        (seq? form)) (doall (map pd-single form))
    :else (throw (Exception. (str "Not any recognizable form: " form)))))

(defn pd
  [& forms]
  (let [r (doall (map pd-single forms))]
    (if (> (count r) 1)
      r
      (first r))))

(defn outlet
  "Use OUTLET to specify the intended outlet of a connection. 
  E.g. `(pd [:+ (outlet (pd [:moses ...]) 1)])`. 
  The default outlet is 0."
  [n node]
  (assert (number? n))
  (assoc (pd node) :outlet n))

(defn inlet
  "Use INLET to specify the intended inlet for a connection.
  E.g. `(pd [:/ 1 (inlet (pd ...) 1)])`. The default inlet is determined
  by the source node argument position (not counting literals, only
  NIL and other nodes) (e.g. 0 in the previous example)."
  [n node]
  (assert (number? n))
  (assoc (pd node) :inlet n))

(defn other
  "An OTHER is a special node that refers to another node.
  It is a placeholder for the node with `:name` = NAME in its `:options`
  map. It is instrumental to craft mutually connected nodes, and can
  be used to reduce the number of LETs in patch definitions.  OTHER
  nodes are de-referenced after the entire patch has been walked, so
  forward reference is possible.

  Examples:

  ```clojure
  ;; connecting the same node to 2 inlets
  (pd [:osc- {:name \"foo\"} 200])
  (pd [:dac- (other \"foo\") (other \"foo\")])
  ```

  ```clojure
  ;; circular connections
  (pd [:float {:name 'f} [:msg \"bang\"] [:+ 1 (other 'f)]])
  ```

  ```clojure
  ;; connecting to nodes ahead of their definition
  (pd [:float {:name 'f} [:msg \"bang\"] (other '+)])
  (pd [:+ {:name '+} 1 (other 'f)])
  ```"
  [name]
  {:type :node
   :other name})
