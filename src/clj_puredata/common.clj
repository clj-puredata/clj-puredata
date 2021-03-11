(ns clj-puredata.common
  "Helper predicates for identifying common constructs.")

(defn hiccup?
  [form]
  (and (vector? form)
       (or (keyword? (first form))
           (string? (first form)))))

(defn literal?
  "Returns `true` for numbers, strings and `nil`."
  [arg]
  (if (or (number? arg)
          (string? arg)
          (char? arg)
          (nil? arg))
    true
    false))

(defn node?
  "Identifies maps whose `:type` equals `:node`. "
  [arg]
  (and (map? arg)
       (= (:type arg) :node)))

(defn connection?
  "Identifies maps whose `:type` equals `:connection`."
  [arg]
  (and (map? arg)
       (= (:type arg) :connection)))

(defn other?
  "Check if a node is a placeholder or duplicate (it has no `:id`, but references an `:other`).
  Also see [[other]]."
  [arg]
  (and (map? arg)
       (= (:type arg) :other)
       ;;(nil? (:id node))
       (some? (:other arg))))
