(ns clj-puredata.layout
  "Rudimentary graph layouter for arranging nodes, to simplify debugging of generated patches. "
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
        ;; FIXME: this map contains key `nil` when some nodes have not been processed (e.g. circularly connected ones)
        ;; this causes `(count cols)` to be 1 higher than the actual column count
        ;; and `(cols (count cols))` will return `nil` (because the numeric key isn't found).
        ;; --> TODO: improve algo lol - comb tree and remove circles from graph
        ;; FIXME: this map also might skip indices, and those will be dropped quietly, and this will result in the `col-lens` array being too short because some nodes will still index the high column and cause out-of-bounds when accessing the result of this function.
        ;; --> FIXED: sorted-map instead of vector
        node-name-len #(max 5 (min 25 (inc (count (clojure.string/join " " (cons (:op %) (:args %)))))))
        col-lens (for [n (sort (keys cols)) ;;(range (count cols))
                       :let [col (cols n)] ; assure correct order
                       :when (some? col)]  ; prevent nil
                   (apply max (map node-name-len col)))
        col-indents (reduce (fn [a b] (conj a (+ (last a) b)))
                            [0]
                            col-lens)
        col-indents-scaled (mapv #(* % 7) col-indents)]
    (into (sorted-map) (zipmap (sort (filter some? (keys cols)))
                               col-indents-scaled))))

(defn manually-positioned?
  [n]
  (or (get-in n [:options :x])
      (get-in n [:options :y])))

(defn auto-position
  [n & {:keys [col-pos row-pos
               x-offset y-offset]
        :or {col-pos (fn [n] (* n 100))
             row-pos (fn [n] (* n 40))
             x-offset 5 y-offset 5}}]
  (-> n
      (update-in [:options :y] (fn [y] (if (some? y) y (+ y-offset (row-pos (:row n 0))))))
      (update-in [:options :x] (fn [x] (if (some? x) x (+ x-offset (col-pos (:col n 0))))))
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
        ids (set (range (count nodes))) ;; assumes that node ids are dispensed sequentially, starting at 0
        child-nodes (set (map #(get-in % [:to-node :id]) connections))
        top-nodes (sort (clojure.set/difference ids child-nodes))] ;; FIXME: fails to find top node on completely circular patches (pd 3/7/2021)
    (reset! visited-nodes [])
    (reset! min-col 0)
    (reset! node-map (into {} (map #(vector (:id %) %) nodes)))
    (reset! connections-map (group-by #(get-in % [:from-node :id]) connections))
    ;;
    (doall (map run-chain top-nodes))
    ;; TODO: check for non-traversed nodes (e.g. circularly connected) and traverse them in some way. (pd 3/29/2021)
    ;; (e.g. pick at random, traverse, check again if any untraversed remain)
    (convert-rows!)
    ;;
    (into (sorted-nodes) connections)))
