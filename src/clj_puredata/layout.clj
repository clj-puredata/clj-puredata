(ns clj-puredata.layout
  (:require [clj-puredata.common :refer :all]))

(def visited-nodes (atom []))

(def node-map (atom {})) ;; map of nodes, keyed by node id

(def connections-map (atom {})) ;; map of connections, keyed by node id of originating node

(defn set-score!
  [node score]
  ;; only assign new score when higher (push nodes down, never up)
  (swap! node-map
         update-in [node :score]
         #(if (or (nil? %)
                  (> score %))
            score
            %)))

(defn visited-before?
  [node]
  (.contains @visited-nodes node))

(defn child-nodes
  [node]
  (mapv #(get-in % [:to-node :id])
        (@connections-map node)))

(defn run-chain
  "Follow the children of a given node, assigning increasing scores to them.
  Take care not to re-assign nodes that are parents of the current node;
  but feel free to increase the score of other previously visited nodes."
  [node & {:keys [score parent-chain]
           :or {score 0
                parent-chain []}}]
  (when-not (visited-before? node)
    (swap! visited-nodes conj node)
    (when-not (.contains parent-chain node)
      (set-score! node score))
    (doall (map #(run-chain %
                            :score (inc score)
                            :parent-chain (conj parent-chain node))
                (child-nodes node)))))

(defn convert-scores!
  "Use SCORE property to assign :Y position to nodes."
  []
  (swap! node-map
         #(->> %
               (map (fn [[k v]]
                      [k (-> v
                             (assoc-in [:options :y] (* (:score v) 40))
                             (assoc-in [:options :x] (rand-int 200)))]))
               (into {}))))

(defn sorted-nodes
  []
  (vec (for [n (range (count @node-map))]
         (@node-map n))))

(defn layout-lines
  [lines]
  (let [nodes (filter node? lines)
        connections (filter connection? lines)
        ids (set (range (count nodes)))
        child-nodes (set (map #(get-in % [:to-node :id]) connections))
        top-nodes (clojure.set/difference ids child-nodes)]
    (reset! visited-nodes [])
    (reset! node-map (into {} (map #(vector (:id %) %) nodes)))
    (reset! connections-map (group-by #(get-in % [:from-node :id]) connections))
    (doall (map run-chain top-nodes))
    ;;(clojure.pprint/pprint @node-map)
    (convert-scores!)
    ;;(clojure.pprint/pprint @node-map)
    (into (sorted-nodes) connections)))
