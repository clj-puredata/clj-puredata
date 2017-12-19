(ns clj-puredata.translate
  (:require [clojure.string :as string]))

(def obj-nodes #{"+" "-" "*" "/"})
(def self-nodes #{"msg" "text"})

(def default-options
  {obj-nodes {:x 0 :y 0}
   self-nodes {:x 0 :y 0}})

(def templates
  {obj-nodes ["#X" "obj" :x :y :op]
   self-nodes ["#X" :op :x :y :args]})

(defn merge-options [defaults n]
  (assoc n :options (merge defaults (:options n))))

(defn fill-template [t n]
  (->
   (string/join
    " "
    (for [lookup t]
      (if (string? lookup)
        lookup
        (let [elm (cond (keyword? lookup) (or (lookup (:options n))
                                              (lookup n))
                        (vector? lookup) (get-in n lookup))]
          (if (coll? elm)
            (string/join " " (map str elm))
            (str elm))))))
   (str ";")))

(defn translate-connection [c]
  (fill-template ["#X" "connect"
                  [:from-node :id] [:from-node :outlet]
                  [:to-node :id] [:to-node :inlet]] c))

(defn translate-node [n]
  (loop [[k & rst] (keys templates)]
    (cond (nil? k) nil
          (k (:op n)) (->> n
                           (merge-options (default-options k))
                           (fill-template (templates k)))
          :else (recur rst))))
