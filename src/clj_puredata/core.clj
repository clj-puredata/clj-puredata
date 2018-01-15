(ns clj-puredata.core
  (:require [clj-puredata.parse :as parse]
            [clj-puredata.translate :as translate]
            [clj-puredata.comms :as comms]
            [potemkin :refer [import-vars]]))

(import-vars
 [clj-puredata.parse
  pd
  inlet
  outlet
  other]
 [clj-puredata.translate
  with-patch]
 [clj-puredata.comms
  open-pd
  load-patch])

(comment
  ;; 1 - create a basic patch using WITH-PATCH.
  (with-patch "test.pd"
    {:width 800 :height 800}
    (pd [:text "Hello World"]))
  ;; 2 - open PureData.
  (open-pd)
  (Thread/sleep 3000)
  ;; 3 - load your patch.
  (load-patch "test.pd")
  ;; 4 - now edit the original WITH-PATCH, evaluate it, and see PureData update accordingly.
  ;; 5 - rinse and repeat.
  ) 
