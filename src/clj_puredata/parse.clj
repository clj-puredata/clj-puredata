(ns clj-puredata.parse
  (:require [clojure.test :as t]))

(def parse-context (atom nil))

(defn setup-parse-context []
  (reset! parse-context {:current-node-id 0
                         :patch []
                         :processed-node-ids #{}}))

(defn teardown-parse-context []
  (reset! parse-context nil))

(defn add-element [e]
  "Add NODE to the current PARSE-CONTEXT."
  (swap! parse-context update :patch conj e)
  e)

(defn dispense-node-id []
  (if-let [id (:current-node-id @parse-context)]
    (do (swap! parse-context update :current-node-id inc)
        id)
    -1))

(defn op-from-kw [op-kw]
  "Keyword -> string, e.g. :+ -> \"+\"."
  (subs (str op-kw) 1))

(defn hiccup? [form]
  (and (vector? form)
         (keyword? (first form))))

(defn literal? [arg]
  "Returns TRUE for numbers, strings and NIL."
  (if (or (number? arg) 
          (string? arg)
          (char? arg)
          (nil? arg))
    true
    false))

(defn node? [arg]
  (and (map? arg)
       (= (:type arg) :node)))

(defn sort-patch [patch]
  (->> patch
       (sort-by :id)
       (sort-by (comp :id :from-node))
       vec))

(defn processed? [node]
  ((:processed-node-ids @parse-context) (:id node)))

(defn outlet [node n]
  (assoc node :outlet n))

(defn inlet [node n]
  (assoc node :inlet n))

(defn node-or-explicit-skip? [x]
  (or (node? x) (nil? x)))

(defn walk-tree
  ([node parent inlet]
   (add-element {:type :connection
                 :from-node {:id (:id node)
                             :outlet (:outlet node 0)}
                 :to-node {:id parent
                           :inlet (:inlet node inlet)}})
   (walk-tree node))
  ([node]
   (when (not (processed? node))
     (add-element (update node :args (comp vec (partial remove node?))))
     (swap! parse-context update :processed-node-ids conj (:id node)))
   (let [connected-nodes (filter node-or-explicit-skip? (:args node))]
     (when (not (empty? connected-nodes))
       (doall (map-indexed (fn [i c] (when (node? c) (walk-tree c (:id node) i)))
                           connected-nodes))))))

(defn pd [form]
  (cond
    (hiccup? form)
    (let [op (first form)
          [options args] (if (and (map? (second form))
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
    (fn? form) (form)))

(defmacro with-patch [options & rest]
  (let [forms (if (map? options)
                rest
                (conj rest options))]
    `(do
       (setup-parse-context)
       (let [nodes# (vector ~@forms)]
         (doall (map walk-tree nodes#))
         (let [patch# (sort-patch (:patch @parse-context))]
           ;; (clojure.pprint/pprint @parse-context)
           (teardown-parse-context)
           {:nodes nodes#
            :patch patch#})))))
