(ns clj-puredata.core
  (:require [clj-puredata.parse :as parse]
            [clj-puredata.translate :as translate]
            [clj-puredata.puredata :as puredata]
            [potemkin :refer [import-vars]])
  ;;(:gen-class)
  )

(import-vars
 [clj-puredata.parse
  pd
  inlet
  outlet
  other]
 [clj-puredata.translate
  with-patch]
 [clj-puredata.puredata
  open-pd
  load-patch])

(comment
  ;; 1 - create a basic patch using WITH-PATCH.
  (with-patch "test.pd"
    {:width 800 :height 800}
    (pd [:text "Hello World"]))
  ;; 2 - open PureData.
  (open-pd)
  ;; 3 - load your patch.
  (load-patch "test.pd")
  ;; 4 - now edit the original WITH-PATCH, evaluate it, and see PureData update accordingly.
  ;; 5 - rinse and repeat.
  )

#_(defn -main
    [& args]) 
