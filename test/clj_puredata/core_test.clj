(ns clj-puredata.core-test
  (:require [clojure.test :refer :all]
            [clj-puredata.core :refer :all]
            [clojure.string :as string]))

(deftest default-usage
  (testing "The function write-patch"
    (testing "returns the contents of the given patch, as they would be written to a file."
      (is (let [return-value (write-patch "basic-usage.pd" [:text "hello world"])]
            (and (string? return-value)
                 (string/includes? return-value "hello world")))))
    (testing "writes the patch file to disk."
      (is (let [file-name "empty.pd"
                file-contents (write-patch file-name)]
            (= file-contents (slurp file-name)))))))
