(ns clj-puredata.parse
  (:require [clojure.test :as t]))

(defn parse
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
       :args (mapv parse args)})
    ;;
    (number? form)
    form
    ;;
    (string? form)
    form
    ;;
    :else
    (throw (Exception. "Unknown input to clj-puredata.parse"))))

(t/deftest parser
  (t/testing "simple forms"
    (t/is (= (parse [:+ 1 2])
             {:op "+"
              :options {}
              :args [1 2]})))
  (t/testing "recursion"
    (t/is (= (parse [:+ [:* 2 2] 1])
             {:op "+"
              :options {}
              :args [{:op "*"
                      :options {}
                      :args [2 2]}
                     1]}))))

#_(t/deftest a-test
  (t/testing "FIXME, I fail."
    (t/is (= 0 1))))

