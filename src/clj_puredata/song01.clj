(ns clj-puredata.song01
  (:require [clj-puredata.core :refer [open-pd
                                       load-patch
                                       with-patch
                                       pd
                                       inlet
                                       outlet
                                       other]]))

(with-patch "counter.pd"
  ^{:doc "Increments its output when banged. Starts at 0."}
  (pd (as-> [:+ 1 (other 'f)] %
        [:f {:name 'f} 0 [:b [:inlet]] %]
        [:outlet %])))

(with-patch "beats.pd"
  (pd [:outlet ["counter.pd" [:metro "$1" [:loadbang]]]]))

(with-patch "between.pd"
  ^{:doc "Receives a number and outputs it when it is within [$1..$2[.
Second outlet outputs 1 when number enters range and 0 when it exits range."}
  (pd (as-> [:inlet] %
        [:moses "$1" %]
        [:moses {:name 'm} "$2" (outlet 1 %)]
        [:t {:name 't} "f f" %]
        [:outlet {:x 5} %])
      [:outlet {:x 105}
       [:change [:msg 1 (outlet 1 (other 't))]
        (inlet 0 [:msg 0 (outlet 1 (other 'm))])]]))

(with-patch "between-on-off.pd"
  (pd [:outlet (outlet 1 ["between.pd" "$1" "$2" [:inlet]])]))

(def notes [200 240 400 410])

(defn sound1
  [mod on off note]
  (pd (as-> [:osc- (nth notes note)] %
        (as-> (other 'beats) %2
          [:mod mod %2]
          [:msg "$1 20" ["between-on-off.pd" on off %2]]
          [:line- %2]
          [:*- % %2])
        [:throw- "output" %])))

(with-patch "song01.pd"
  ;; output sum
  (pd [:*- {:name 'dac} 0.1 [:catch- "output"]]
      [:dac- (other 'dac) (other 'dac)])
  ;; sound 1
  (sound1 8 0 4 0)
  (sound1 10 4 7 1)
  (sound1 8 0 1 2)
  (sound1 10 4 5 3)
  ;; main bpm
  (pd ["beats.pd" {:name 'beats} 125])
  ;; misc
  #_(pd ["between.pd" {:name 'bt} 0 4 (other 'beats)]
      [:print "count-between" (other 'bt)]
      [:print "on-off-between" (outlet 1 (other 'bt))]))

(defn startup
  []
  (open-pd)
  (Thread/sleep 3000)
  (load-patch "song01.pd"))

;;(startup)
