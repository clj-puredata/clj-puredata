(ns clj-puredata.parse
  (:require [clojure.test :as t]
            [vijual :as v]))

(def parse-context (atom nil))

(defn setup-parse-context []
  (reset! parse-context {:current-node-id 0
                         :lines []
                         :processed-node-ids #{}}))

(defn teardown-parse-context []
  (reset! parse-context nil))

(defn add-element [e]
  "Add NODE to the current PARSE-CONTEXT."
  (swap! parse-context update :lines conj e)
  e)

(defn dispense-node-id []
  (if-let [id (:current-node-id @parse-context)]
    (do (swap! parse-context update :current-node-id inc)
        id)
    -1))

(defn op-from-kw [op-kw]
  "Keyword -> string, e.g. :+ -> \"+\"."
  (if (keyword? op-kw)
    (subs (str op-kw) 1)
    (str op-kw)))

(defn hiccup? [form]
  (and (vector? form)
       (or (keyword? (first form))
           (string? (first form)))))

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

(defn assoc-layout [layout line]
  (if (node? line)
    (let [fac 60
          pos (first (filter #(= (str (:id line)) (:text %)) layout))]
      (when pos
        (-> line
            (assoc-in [:options :x] (* fac (:x pos)))
            (assoc-in [:options :y] (* fac (:y pos))))))
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

(defn processed? [node]
  ((:processed-node-ids @parse-context) (:id node)))

(defn outlet [node n]
  (assoc node :outlet n))

(defn inlet [node n]
  (assoc node :inlet n))

(defn other [name]
  {:type :node
   :other name})

(defn node-or-explicit-skip? [x]
  (or (node? x) (nil? x)))

(defn other? [node]
  (and (nil? (:id node))
       (some? (:other node))))

(defn resolve-other [other]
  (let [solve (first (filter #(= (:other other) (get-in % [:options :name]))
                             (@parse-context :lines)))]
    (if (nil? solve)
      (throw (Exception. (str "Cannot resolve other node " other)))
      solve)))

(defn walk-tree
  ([node parent inlet]
   (add-element {:type :connection
                 :from-node {:id (if (other? node)
                                   (:id (resolve-other node))
                                   (:id node))
                             :outlet (:outlet node 0)}
                 :to-node {:id parent
                           :inlet (:inlet node inlet)}})
   (when-not (other? node) (walk-tree node)))
  ([node]
   (when (not (processed? node))
     (swap! parse-context update :processed-node-ids conj (:id node))
     (add-element (update node :args (comp vec (partial remove node?))))
     (let [connected-nodes (filter node-or-explicit-skip? (:args node))]
       (when (not (empty? connected-nodes))
         (doall (map-indexed (fn [i c] (when (node? c) (walk-tree c (:id node) i)))
                             connected-nodes)))))))

(defmacro in-context [& forms]
  `(do
     (setup-parse-context)
     (let [nodes# (vector ~@forms)]
       (doall (map walk-tree nodes#))
       (let [lines# (layout-lines (sort-lines (:lines @parse-context)))]
         (teardown-parse-context)
         {:nodes nodes#
          :lines lines#}))))

(defn pd [form]
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
