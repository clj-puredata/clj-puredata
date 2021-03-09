(ns clj-puredata.core
  "Collects all user-facing functions of the other namespaces, for easy import."
  (:require [clj-puredata.parse :as parse]
            [clj-puredata.translate :as translate]
            [clj-puredata.puredata :as puredata]
            [potemkin :refer [import-vars]]))

(import-vars
 [clj-puredata.parse
  pd
  inlet
  outlet
  other]
 [clj-puredata.translate
  write-patch
  write-patch-reload]
 [clj-puredata.puredata
  open-pd
  load-patches
  reload-all-patches
  startup])

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
