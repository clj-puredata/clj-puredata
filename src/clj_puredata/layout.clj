(ns clj-puredata.layout
  (:require [clojure.set]
            [clj-puredata.common :refer :all]))

(def node-map (atom {})) ;; map of nodes, keyed by node id

(def connections-map (atom {})) ;; map of connections, keyed by node id of originating node

(def visited-nodes (atom []))

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

(defn column-indentations
  []
  (let [cols (group-by :col (vals @node-map))
        node-name-len #(count (clojure.string/join " " (cons (:op %) (:args %))))
        col-lens (for [n (range (count cols)) :let [col (cols n)]] ;; assure correct order
                   (apply max (map node-name-len col)))
        col-indents (reduce (fn [a b] (conj a (+ (last a) b)))
                            [0] col-lens)]
    (mapv #(* % 7) col-indents)))

(defn manually-positioned?
  [n]
  (and (get-in n [:options :x])
       (get-in n [:options :y])))

(defn auto-position
  [n & {:keys [col-pos row-pos
               x-offset y-offset]
        :or {col-pos (fn [n] (* n 100))
             row-pos (fn [n] (* n 40))
             x-offset 5 y-offset 5}}]
  (-> n
      (update-in [:options :y] (fn [y] (if (some? y) y (+ y-offset (row-pos (:row n))))))
      (update-in [:options :x] (fn [x] (if (some? x) x (+ x-offset (col-pos (:col n))))))
      (assoc :auto-layout true)))

(defn convert-rows!
  "Use ROW and COL properties to assign :Y and :X position to nodes."
  []
  (let [col-indents (column-indentations)]
    (swap! node-map
           #(->> %
                 (map (fn [[k v]]
                        (if (manually-positioned? v)
                          [k v]
                          [k (auto-position v :col-pos col-indents)])))
                 (into {})))))

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
        top-nodes (sort (clojure.set/difference ids child-nodes))] ;; FIXME: fails to find top node on completely circular patches (pd 3/7/2020)
    (reset! visited-nodes [])
    (reset! min-col 0)
    (reset! node-map (into {} (map #(vector (:id %) %) nodes)))
    (reset! connections-map (group-by #(get-in % [:from-node :id]) connections))
    ;;
    (doall (map run-chain top-nodes))
    (convert-rows!)
    ;;
    (into (sorted-nodes) connections)))
