(ns clj-puredata.core-test
  (:require [clojure.test :refer :all]
            [clj-puredata.core :refer :all]))

(t/deftest basic
  (t/testing "Parsing"
    (t/testing "a simple form."
      (t/is (= (parse [:+ 1 2])
               [{:type ::node :op "+" :id 0
                 :options {} :args [1 2]}])))
    (t/testing "recursively, which triggers connections."
      (t/is (= (parse [:+ [:* 2 2] 1])
               [{:type ::node :op "+" :id 0
                 :options {} :args [1]}
                {:type ::node :op "*" :id 1
                 :options {} :args [2 2]}
                {:type ::connection
                 :from-node {:id 1 :outlet 0}
                 :to-node {:id 0 :inlet 0}}])))
    (t/testing "will skip target inlet when argument is NIL."
      (t/is (= (parse [:+ nil [:*]])
               [{:type ::node :op "+" :id 0
                 :options {} :args []}
                {:type ::node :op "*" :id 1
                 :options {} :args []}
                {:type ::connection
                 :from-node {:id 1 :outlet 0}
                 :to-node {:id 0 :inlet 1}}])))
    (t/testing "can adjust target inlet."
      (t/is (= (parse [:+ (inlet [:*] 1)])
               [{:type ::node :op "+" :id 0
                 :options {} :args []}
                {:type ::node :op "*" :id 1
                 :options {} :args []}
                {:type ::connection
                 :from-node {:id 1 :outlet 0}
                 :to-node {:id 0 :inlet 1}}])))
    (t/testing "can adjust source outlet."
      (t/is (= (parse [:+ (outlet [:*] 1)])
               [{:type ::node :op "+" :id 0
                 :options {} :args []}
                {:type ::node :op "*" :id 1
                 :options {} :args []}
                {:type ::connection
                 :from-node {:id 1 :outlet 1}
                 :to-node {:id 0 :inlet 0}}])))))

(t/deftest tricky
  (t/testing "Tricky parsing"
    (t/testing "accomodates the use of LET."
      (t/is (= (parse (let [x [:* 2 2]] [:+ x x]))
               [{:type ::node :op "+" :id 0
                 :options {} :args []}
                {:type ::node :op "*" :id 1
                 :options {} :args [2 2]}
                {:type ::connection
                 :from-node {:id 1 :outlet 0}
                 :to-node {:id 0 :inlet 0}}
                {:type ::connection
                 :from-node {:id 1 :outlet 0}
                 :to-node {:id 0 :inlet 1}}])
            "This might be a bit motivated - does this require a macro that walks the form, distinguishing hiccup and regular clojure? ")
      (t/is (= (pd-patch
                (let [x (pd [:* 2 2])]
                  (pd [:+ x x]))))
            "This seems more practical - #'pd returns a map (still using unique ids for nodes), constructs a tree with duplicates, and sort them out later."))))
