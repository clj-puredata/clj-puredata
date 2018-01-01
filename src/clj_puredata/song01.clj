(ns clj-puredata.song01
  (:require [clj-puredata.core :refer [open-pd
                                       load-patch
                                       with-patch
                                       pd
                                       inlet
                                       outlet
                                       other]]))

(with-patch "counter.pd"
  (pd (as-> [:+ 1 (other 'f)] %
        [:f {:name 'f} 0 [:b [:inlet]] %]
        [:outlet %])))

(with-patch "beats.pd"
  (pd [:outlet ["counter.pd" [:metro "$1" [:loadbang]]]]))

(with-patch "between.pd"
  (pd (as-> [:inlet] %
        [:moses "$1" %]
        [:moses "$2" (outlet 1 %)]
        [:outlet %])))

(with-patch "song01.pd"
  (pd [:print ["between.pd" 0 4 ["beats.pd" 125]]]))

(defn startup
  []
  (open-pd)
  (Thread/sleep 3000)
  (load-patch "song01.pd"))

;;(startup)
