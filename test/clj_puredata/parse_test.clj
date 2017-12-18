(ns clj-puredata.parse-test
  (:require [clj-puredata.parse :refer :all]
            [clojure.test :refer :all]))

(deftest basic
  (testing "Parsing"
    (testing "a simple form."
      (is (= (parse [:+ 1 2])
             [{:type :clj-puredata.parse/node :op "+" :id 0
               :options {} :args [1 2]}])))
    (testing "recursively, which triggers connections."
      (is (= (parse [:+ [:* 2 2] 1])
             [{:type :clj-puredata.parse/node :op "+" :id 0
               :options {} :args [1]}
              {:type :clj-puredata.parse/node :op "*" :id 1
               :options {} :args [2 2]}
              {:type :clj-puredata.parse/connection
               :from-node {:id 1 :outlet 0}
               :to-node {:id 0 :inlet 0}}])))
    (testing "will skip target inlet when argument is NIL."
      (is (= (parse [:+ nil [:*]])
             [{:type :clj-puredata.parse/node :op "+" :id 0
               :options {} :args []}
              {:type :clj-puredata.parse/node :op "*" :id 1
               :options {} :args []}
              {:type :clj-puredata.parse/connection
               :from-node {:id 1 :outlet 0}
               :to-node {:id 0 :inlet 1}}])))
    (testing "can adjust target inlet."
      (is (= (parse [:+ (inlet [:*] 1)])
             [{:type :clj-puredata.parse/node :op "+" :id 0
               :options {} :args []}
              {:type :clj-puredata.parse/node :op "*" :id 1
               :options {} :args []}
              {:type :clj-puredata.parse/connection
               :from-node {:id 1 :outlet 0}
               :to-node {:id 0 :inlet 1}}])))
    (testing "can adjust source outlet."
      (is (= (parse [:+ (outlet [:*] 1)])
             [{:type :clj-puredata.parse/node :op "+" :id 0
               :options {} :args []}
              {:type :clj-puredata.parse/node :op "*" :id 1
               :options {} :args []}
              {:type :clj-puredata.parse/connection
               :from-node {:id 1 :outlet 1}
               :to-node {:id 0 :inlet 0}}])))))

(deftest tricky
  (testing "Tricky parsing"
    (testing "accomodates the use of LET."
      (is (= (parse (let [x [:* 2 2]] [:+ x x]))
             [{:type :clj-puredata.parse/node :op "+" :id 0
               :options {} :args []}
              {:type :clj-puredata.parse/node :op "*" :id 1
               :options {} :args [2 2]}
              {:type :clj-puredata.parse/connection
               :from-node {:id 1 :outlet 0}
               :to-node {:id 0 :inlet 0}}
              {:type :clj-puredata.parse/connection
               :from-node {:id 1 :outlet 0}
               :to-node {:id 0 :inlet 1}}])
          "This might be a bit motivated - does this require a macro that walks the form, distinguishing hiccup and regular clojure? ")
      #_(is (= (pd-patch
                (let [x (pd [:* 2 2])]
                  (pd [:+ x x]))))
            "This seems more practical - #'pd returns a map (still using unique ids for nodes), constructs a tree with duplicates, and sort them out later."))))

(deftest construct-tree
  (testing "The function PD"
    (testing "will expand hiccup [:+ 1 2] to maps {:type ::node ...}."
      (is (= (pd [:+ 1 2 3])
             {:type :clj-puredata.parse/node
              :op "+" :id -1
              :options {} :args [1 2 3]})))
    (testing "will pass along maps in second position as options."
      (is (= (pd [:+ {:an-option true} 1])
             {:type :clj-puredata.parse/node
              :op "+" :id -1
              :options {:an-option true} :args [1]})))
    (testing "also works recursively."
      (is (= (pd [:+ [:- 3 2] 1])
             {:type :clj-puredata.parse/node
              :op "+" :id -1
              :options {} :args [{:type :clj-puredata.parse/node
                                  :op "-" :id -1
                                  :options {} :args [3 2]}
                                 1]})))
    (testing "will assign indices when wrapped in #'WITH-PATCH."
      (is (= (with-patch (pd [:+ 1 2 3]))
             [{:type :clj-puredata.parse/node
               :op "+" :id 0
               :options {} :args [1 2 3]}])))
    (testing "will assign indices recursively (depth-first, in left-to-right argument order)"
      (is (= (with-patch (pd [:+ [:- [:*]] [:/]]))
             [{:type :clj-puredata.parse/node
               :op "+" :id 0
               :options {} :args [{:type :clj-puredata.parse/node
                                   :op "-" :id 1
                                   :options {} :args [{:type :clj-puredata.parse/node
                                                       :op "*" :id 2
                                                       :options {} :args []}]}
                                  {:type :clj-puredata.parse/node
                                   :op "/" :id 3
                                   :options {} :args []}]}])))
    (testing "intentionally preserves the indices of duplicate nodes in tree."
      (is (= (with-patch
               (let [x (pd [:+])]
                 (pd [:* x x])))
             [{:type :clj-puredata.parse/node
               :op "*" :id 1
               :options {} :args [{:type :clj-puredata.parse/node :op "+" :id 0 :options {} :args []}
                                  {:type :clj-puredata.parse/node :op "+" :id 0 :options {} :args []}]}])))))

#_(deftest walk-tree
  (testing "The function WALK-TREE"
    (testing "")))
