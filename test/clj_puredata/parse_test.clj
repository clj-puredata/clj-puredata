(ns clj-puredata.parse-test
  (:require [clj-puredata.parse :refer :all]
            [clojure.test :refer :all]
            [clojure.set :refer [subset?]]))

(defn assure-no-context
  [f]
  (teardown-parse-context)
  (f))

(use-fixtures :each assure-no-context)

(deftest constructing-the-tree
  (testing "The function PD"
    (testing "will expand hiccup [:+ 1 2] to maps {:type ::node ...}."
      (is (subset? (set {:type :node
                         :op "+"
                         :options {} :args [1 2 3]})
                   (set (pd [:+ 1 2 3])))))
    (testing "will pass along maps in second position as options."
      (is (-> (pd [:+ {:an-option true}]) :options :an-option)))
    (testing "also works recursively."
      (is (let [x (pd [:+ [:-]])]
            (and (= (:op x) "+")
                 (= (-> x :args first :op) "-")))))
    (testing "will assign indices when wrapped in #'IN-CONTEXT."
      (is (subset? (set {:type :node :op "+" :id 0})
                   (-> (in-context (pd [:+ 1 2 3])) :lines first set))))
    (testing "will assign indices recursively (depth-first, in left-to-right argument order)"
      (is (every? identity (map #(subset? (set %1) (set %2))
                                [{:op "+" :id 0}
                                 {:op "-" :id 1}
                                 {:op "*" :id 2}
                                 {:op "/" :id 3}]
                                (-> (in-context (pd [:+ [:- [:*]] [:/]])) :lines)))))
    (testing "intentionally preserves the indices of duplicate nodes in tree."
      (is (let [x (pd [:+])
                y (pd [:* x x])]
            (apply = (map :unique-id (:args y))))))))

(deftest walking-the-tree
  (testing "The function WALK-TREE"
    (testing "writes nodes out into the :LINES field of atom PARSE-CONTEXT."
      (is (= (-> (in-context (pd [:+]) (pd [:-]))
                 :lines count)
             2)))
    (testing "creates connections when nodes have other nodes as arguments."
      (is (= (-> (:lines (in-context (pd [:+ [:-]])))
                 (nth 2) :type)
             :connection)))
    (testing "skips inlet if NIL is supplied as an argument."
      (is (= (-> (:lines (in-context (pd [:+ nil [:-]])))
                 (nth 2) :to-node :inlet)
             1)))
    (testing "respects the keys set by #'OUTLET and #'INLET and modifies connections accordingly."
      (is (let [p (:lines (in-context (pd [:+ (outlet 23 (pd [:*])) (inlet 42 (pd [:/]))])))]
            (and (= (-> p (nth 3) :from-node :outlet) 23)
                 (= (-> p (nth 4) :to-node :inlet) 42)))))))
