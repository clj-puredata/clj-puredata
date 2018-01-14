(ns clj-puredata.layout
  (:require [clj-puredata.common :refer :all]))

(def visited-nodes (atom []))

(def node-map (atom {})) ;; map of nodes, keyed by node id

(def connections-map (atom {})) ;; map of connections, keyed by node id of originating node

(def min-col (atom 0)) ;; minimum indentation for next call of run-chain

(defn set-row!
  "Only assigns new row when higher (push nodes down, never up)."
  [node row]
  (swap! node-map
         update-in [node :row]
         #(if (or (nil? %) (> row %)) row %)))

(defn set-col!
  [node col]
  (swap! node-map assoc-in [node :col] col))

(defn visited-before?
  [node]
  (.contains @visited-nodes node))

(defn child-nodes
  "Return the :id of all nodes connected to NODE by outgoing connections."
  [node]
  (mapv #(get-in % [:to-node :id])
        (sort-by (comp :outlet :from-node)
                 (@connections-map node))))

(defn run-chain
  "Follow the children of a given node, assigning increasing rows to them.
  Take care not to re-assign nodes that are parents of the current node;
  but feel free to increase the row of other previously visited nodes."
  [node & {:keys [row col parent-chain]
           :or {row 0
                col @min-col
                parent-chain []}}]
  (when-not (.contains parent-chain node)
    (set-row! node row)
    (when-not (visited-before? node)
      (set-col! node col)
      (swap! min-col #(if (>= col %) (inc col) %))
      (swap! visited-nodes conj node))
    (doall (map-indexed #(run-chain %2
                                    :row (inc row)
                                    :col (+ col %1)
                                    :parent-chain (conj parent-chain node))
                        (child-nodes node)))))

(defn convert-rows!
  "Use ROW property to assign :Y position to nodes."
  []
  (swap! node-map
         #(->> %
               (map (fn [[k v]]
                      [k (-> v
                             (assoc-in [:options :y] (* (:row v) 40))
                             (assoc-in [:options :x] (* (:col v) 100)))]))
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
        top-nodes (sort (clojure.set/difference ids child-nodes))]
    (reset! visited-nodes [])
    (reset! min-col 0)
    (reset! node-map (into {} (map #(vector (:id %) %) nodes)))
    (reset! connections-map (group-by #(get-in % [:from-node :id]) connections))
    (doall (map run-chain top-nodes))
    (convert-rows!)
    (into (sorted-nodes) connections)))
