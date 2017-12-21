(ns clj-puredata.translate
  (:require [clojure.string :as string]))

(def obj-nodes #{"+" "-" "*" "/"})
(def self-nodes #{"msg" "text"})

(def default-options
  {obj-nodes {:x 0 :y 0}
   self-nodes {:x 0 :y 0}})

(def node-templates
  {obj-nodes ["#X" "obj" :x :y :op]
   self-nodes ["#X" :op :x :y :args]})

(def connection-template ["#X" "connect" [:from-node :id] [:from-node :outlet] [:to-node :id] [:to-node :inlet]])
(def patch-header-template ["#N" "canvas" :x :y :width :height 10])
(def patch-footer-template ["#X" "coords" "0 1 100 -1 200 140 1"]) ;; TODO figure out later
(def subpatch-footer-template ["#X" "restore" "128 184" name]) ;; TODO figure out later

(defn merge-options [defaults n]
  (assoc n :options (merge defaults (:options n))))

(defn- to-string [elm]
  (if (coll? elm)
    (string/join " " (map str elm))
    (str elm)))

(defn fill-template [t n]
  (->
   (string/join
    " "
    (for [lookup t]
      (cond (string? lookup) lookup
            (number? lookup) (str lookup)
            (keyword? lookup) (to-string (or (lookup (:options n)) (lookup n)))
            (vector? lookup) (to-string (get-in n lookup)))))
   (str ";")))

(defn translate-node [n]
  (loop [[k & rst] (keys node-templates)]
    (cond (nil? k) nil
          (k (:op n)) (->> n
                           (merge-options (default-options k))
                           (fill-template (node-templates k)))
          :else (recur rst))))

(defn translate-any [template x] (fill-template template x))

(defn translate-line [l]
  (condp (fn [te e] (= te (:type e))) l
    :node (translate-node l)
    :connection (translate-any connection-template l)
    :patch-header (translate-any patch-header-template l)
    :patch-footer (translate-any patch-footer-template l)
    :subpatch-footer (translate-any subpatch-footer-template l)))
