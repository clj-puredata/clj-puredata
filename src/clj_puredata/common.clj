(ns clj-puredata.common)

(defn hiccup?
  [form]
  (and (vector? form)
       (or (keyword? (first form))
           (string? (first form)))))

(defn literal?
  "Returns TRUE for numbers, strings and NIL."
  [arg]
  (if (or (number? arg)
          (string? arg)
          (char? arg)
          (nil? arg))
    true
    false))

(defn node?
  [arg]
  (and (map? arg)
       (= (:type arg) :node)))

(defn connection?
  [arg]
  (and (map? arg)
       (= (:type arg) :connection)))

(defn other?
  "See OTHER."
  [node]
  (and (nil? (:id node))
       (some? (:other node))))
