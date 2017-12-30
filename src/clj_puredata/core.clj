(ns clj-puredata.core
  (:require [clj-puredata.parse :refer [pd
                                        inlet
                                        outlet
                                        other]]
            [clj-puredata.translate :refer [with-patch]]
            [clj-puredata.puredata :refer [open-pd
                                           load-patch]])
  (:gen-class))

;; 1 - create a basic patch using WITH-PATCH.
(with-patch "test.pd"
  {:width 800 :height 800}
  (pd [:text "Hello World"]))

(comment
  ;; 2 - open PureData.
  (open-pd)
  ;; 3 - load your patch.
  (load-patch "test.pd")
  ;; 4 - now edit the original WITH-PATCH, evaluate it, and see PureData update accordingly.
  ;; 5 - rinse and repeat.
  )

(defn -main
  [& args]) 
