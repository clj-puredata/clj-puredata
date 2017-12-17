(ns clj-puredata.parse
  (:require [clojure.test :as t]))

(defn parse-old
  [form]
  (cond
    (and (vector? form)
         (keyword? (first form)))
    (let [op (first form)
          [options args] (if (map? (second form))
                           [(second form) (drop 2 form)]
                           [{} (rest form)])]
      {:op (subs (str (first form)) 1)
       :options options
       :args (mapv parse-old args)})
    ;;
    (number? form)
    form
    ;;
    (string? form)
    form
    ;;
    :else
    (throw (Exception. "Unknown input to clj-puredata.parse-old"))))

(t/deftest parse-old-test
  (t/testing "simple forms"
    (t/is (= (parse-old [:+ 1 2])
             {:op "+"
              :options {}
              :args [1 2]})))
  (t/testing "recursion"
    (t/is (= (parse-old [:+ [:* 2 2] 1])
             {:op "+"
              :options {}
              :args [{:op "*"
                      :options {}
                      :args [2 2]}
                     1]}))))

;; --------------------------------------------------------------------------------

(def parse-context (atom nil))

(defn setup-parse-context []
  (reset! parse-context {:current-node-id 0
                         :patch []}))

(defn teardown-parse-context []
  (reset! parse-context nil))

(defn add-element [e]
  "Add NODE to the current PARSE-CONTEXT."
  (swap! parse-context update :patch conj e)
  e)

(defn dispense-node-id []
  (let [id (:current-node-id @parse-context)]
    (swap! parse-context update :current-node-id inc)
    id))

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
          (nil? arg))
    true
    false))

(defn node? [arg]
  (and (map? arg)
       (= (:type arg) ::node)))

(declare parse-element)

(defn recur-on-node-args [args id inlet & {:keys [acc] :or {acc []}}]
  "Makes sure that literal arguments (to nodes) are passed verbatim
  while those of type ::node and ::outlet will create a new connection
  to the correct inlet."
  (if (empty? args)
    acc
    (let [arg (parse-element (first args))]
      (cond
        (node? arg)
        (do (add-element {:type ::connection
                          :from-node {:id (:id arg) :outlet 0}
                          :to-node {:id id :inlet inlet}})
            (recur-on-node-args (rest args) id (inc inlet)
                                :acc acc))
        ;;
        ;; (outlet? ... ) -> same as above
        ;;
        :else
        (recur-on-node-args (rest args) id inlet
                            :acc (conj acc arg))))))

(defn parse-element [form]
  (cond
    (hiccup? form) ;; hiccup syntax detected -> create new node
    (let [op (first form)
          [options args] (if (map? (second form))
                           [(second form) (drop 2 form)]
                           [{} (rest form)])
          op (op-from-kw (first form))
          id (dispense-node-id)
          parsed-args (recur-on-node-args args id 0)
          node {:type ::node :op op :id id :options options :args parsed-args}]
      (add-element node))
    ;;
    (literal? form) form))

(defn sort-patch [patch]
  (->> patch
       (sort-by :id)
       (sort-by (comp :id :from-node))))

(defn parse [form]
  (setup-parse-context)
  (parse-element form)
  (let [patch (sort-patch (:patch @parse-context))]
    (teardown-parse-context)
    patch))

(t/deftest parser
  (t/testing "simple forms"
    (t/is (= (parse [:+ 1 2])
             [{:type ::node :op "+" :id 0
               :options {} :args [1 2]}])))
  (t/testing "recursion"
    (t/is (= (parse [:+ [:* 2 2] 1])
             [{:type ::node :op "+" :id 0
               :options {} :args [1]}
              {:type ::node :op "*" :id 1
               :options {} :args [2 2]}
              {:type ::connection
               :from-node {:id 1 :outlet 0}
               :to-node {:id 0 :inlet 0}}])))
  #_(t/testing "outlets"))

