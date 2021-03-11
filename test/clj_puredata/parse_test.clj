(ns clj-puredata.parse-test
  (:require [clj-puredata.parse :refer :all]
            [clj-puredata.common :refer :all]
            [clojure.test :refer :all]
            [clojure.set :refer [subset?]]))

(defn assure-no-context
  [f]
  (teardown-parse-context)
  (f))

(use-fixtures :each assure-no-context)

(deftest constructing-the-tree
  (testing "The function PD"
    (testing "expands hiccup to node."
      (is (node? (pd [:+]))))
    (testing "returns list of nodes when called with multiple arguments"
      (is (let [output (pd [:+] [:-])]
            (and (seq? output)
                 (every? node? output)))))
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
    (testing "intentionally preserves the indices of duplicate nodes in tree."
      (is (let [x (pd [:+])
                y (pd [:* x x])]
            (apply = (map :unique-id (:args y))))))
    (testing "deals with user-made connections from CONNECT"
      (is (user-connection? (pd (connect [:+] [:-])))))))

(deftest walking-the-tree
  (testing "The function LINES"
    (testing "writes nodes out sequentially"
      (is (= (count (lines (pd [:+] [:-])))
             2)))
    (testing "will assign successive indices."
      (is (every? true? (map #(subset? (set %1) (set %2))
                             [{:op "+" :id 0}
                              {:op "-" :id 1}]
                             (lines (pd [:+] [:-]))))))
    (testing "will assign indices recursively (depth-first, in left-to-right argument order)."
      (is (every? true? (map #(subset? (set %1) (set %2))
                             [{:op "+" :id 0}
                              {:op "-" :id 1}
                              {:op "*" :id 2}
                              {:op "/" :id 3}]
                             (lines (pd [:+ [:- [:*]] [:/]])))))) 
    (testing "creates connections when nodes have other nodes as arguments."
      (is (= (-> (lines (pd [:+ [:-]]))
                 (nth 2) :type)
             :connection)))
    (testing "skips inlet if NIL is supplied as an argument."
      (is (= (-> (lines (pd [:+ nil [:-]]))
                 (nth 2) :to-node :inlet)
             1)))
    (testing "respects the keys set by #'OUTLET and #'INLET and modifies connections accordingly."
      (is (let [p (lines (pd [:+ (outlet [:*] 23) (inlet [:/] 42)]))]
            (and (= (-> p (nth 3) :from-node :outlet) 23)
                 (= (-> p (nth 4) :to-node :inlet) 42)))))
    (testing "resolves OTHER correctly"
      (is (let [connection (last (lines (pd [:bang {:name 'b}]
                                            [:print (other 'b)])))]
            (and (= 0 (-> connection :from-node :id))
                 (= 1 (-> connection :to-node :id))))))))

(deftest testing-inlet
  (testing "The function INLET"
    (testing "changes the inlet that a node connects to."
      (is (let [the-connection (last (lines (pd [:+ (inlet [:f 1] 1)])))]
            (and (= (:type the-connection) :connection)
                 (= (-> the-connection :to-node :inlet) 1)))))
    (testing "accepts hiccup, nodes and other."
      (is (let [connections (filter #(= (:type %) :connection)
                                    (lines (pd [:pack "f f f"
                                                (inlet [:f 1] 0)
                                                (inlet (pd [:f 2]) 1)
                                                (inlet (other 'f) 2)]
                                               [:f {:name 'f} 3])))]
            (every? true? (map #(= (-> %1 :to-node :inlet) %2)
                               connections
                               [0 1 2])))))))

(deftest testing-outlet
  (testing "The function OUTLET"
    (testing "changes the outlet that a node connects from."
      (is (let [the-connection (last (lines (pd [:+ (outlet [:f 1] 1)])))]
            (and (= (:type the-connection) :connection)
                 (= (-> the-connection :from-node :outlet) 1)))))
    (testing "accepts hiccup, nodes and other."
      (is (let [connections (filter #(= (:type %) :connection)
                                    (lines (pd [:pack "f f f"
                                                (outlet [:route 1 2] 1)
                                                (outlet (pd [:route 3 4]) 1)
                                                (outlet (other 'r) 1)]
                                               [:route {:name 'r} 5 6])))]
            (every? true? (map #(= (-> %1 :from-node :outlet) %2)
                               connections
                               [1 1 1])))))))

(deftest testing-connect
  (testing "The function CONNECT"
    (testing "fulfills its own predicate."
      (is (user-connection? (connect [:+] [:-]))))
    (testing "connects any two nodes."
      (is (= :connection (:type (last (lines (pd (connect [:+] [:-]))))))))
    (testing "can deal with other as well."
      (is (= :connection (:type (last (lines (pd [:+ {:name 'plus}]
                                                 [:- {:name 'minus}]
                                                 (connect (other 'plus) (other 'minus)))))))))))
