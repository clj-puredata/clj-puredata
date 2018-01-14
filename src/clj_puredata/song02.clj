(ns clj-puredata.song02
  (:require [clj-puredata.core :refer [open-pd
                                       load-patch
                                       with-patch
                                       pd
                                       inlet
                                       outlet
                                       other]]))

(with-patch "song02.pd"
  {:graph-on-parent true
   :view-width 100
   :view-height 100}
  (pd [:outlet
       [:pack "f" "f"
        [:t {:name 't} "f" "f" [:msg "1"]]
        [:moses {:name 'm} 4 [:+ 1
                              (outlet 1 (other 't))
                              (other 'c)
                              (inlet 1 (other 'outlier))]]]]
      [:msg {:name 'c} "5" (outlet 1 (other 'm))]
      [:msg {:name 'outlier} "outlier"]))

(defn startup
  []
  (open-pd)
  (Thread/sleep 3000)
  (load-patch "song02.pd"))

;;(startup)

