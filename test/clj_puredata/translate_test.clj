(ns clj-puredata.translate-test
  (:require  [clj-puredata.translate :refer :all]
             [clojure.test :refer :all]))

(deftest a-test
  (testing "The translation unit"
    (testing "translates single nodes to their PD representation; with merged default-options for :x and :y etc."
      (is (= (translate-node {:type :node
                              :op "+" :id 0
                              :options {} :args []})
             "#X obj 0 0 +;"))
      (is (= (translate-node {:type :node
                              :op "text" :id 0
                              :options {} :args ["hello" "world"]})
             "#X text 0 0 hello world;")))
    (testing "does connections, too!"
      (is (= (translate-connection {:type :connection
                                    :from-node {:id 1 :outlet 2}
                                    :to-node {:id 3 :inlet 4}})
             "#X connect 1 2 3 4;")))))
