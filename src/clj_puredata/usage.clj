(ns clj-puredata.usage
  (:require [clj-puredata.parse :refer [pd
                                        inlet
                                        outlet
                                        other]]
            [clj-puredata.translate :refer [with-patch]]
            [clj-puredata.puredata :refer [open-pd
                                           load]]))
(comment
  (with-patch "wobble.pd"
    {:width 800 :height 800}
    (let [gapper (fn [freq] (pd [:clip- 0 1 [:*- 20 [:osc- freq]]]))
          out (pd [:*- 0.1
                   [:+-
                    (inlet (pd [:*- (gapper 3) [:osc- 500]]) 0)
                    (inlet (pd [:*- (gapper 5) [:osc- 400]]) 0)
                    (inlet (pd [:*- (gapper 7) [:osc- 300]]) 0)
                    (inlet (pd [:*- (gapper 9) [:osc- 200]]) 0)]])]
      (pd ["text" "i'm ignored!"]) ;; let only returns that last value :( ...
      (pd [:dac- out out]))
    (pd [:inlet {:x 0 :y 500}])
    (pd ["text" {:y 400} "i'm in!"])) ;; ... but you can add as many forms as you like :)

  (with-patch "wobble2.pd"
    {:width 800 :height 800}
    (pd [:inlet {:x 0 :y 500}])
    (let [gapper (fn [freq] (pd [:clip- 0 1 [:*- 20 [:osc- freq]]]))]
      (pd [:*- {:name "out"} 0.1
           [:+-
            (inlet (pd [:*- (gapper 3) [:osc- 500]]) 0)
            (inlet (pd [:*- (gapper 5) [:osc- 400]]) 0)
            (inlet (pd [:*- (gapper 7) [:osc- 300]]) 0)
            (inlet (pd [:*- (gapper 9) [:osc- 200]]) 0)]]))
    (pd [:dac- (other "out") (other "out")])) ;; using OTHER, you can reduce the number of LETs in your code.

  (in-context (pd [:+ {:name 0} 1 2])
              (pd [:+ (other 0) (other 0)]))

  (with-patch "ref.pd"
    {:width 800 :height 200}
    (pd [:+ {:name 0} 1 2])
    (pd [:+ (other 0) (other 0)])
    (pd [:text "lol"]))

  (with-patch "huh.pd"
    {:width 800 :height 200}
    (let [a 1]
      (pd [:+ {:y 50} 1 2]))
    (pd [:text "lol"])))

(with-patch "clippercore.pd"
  {:width 800 :height 800}
  (pd [:phasor- {:name "src"} 200])
  (pd [:osc- {:name "fast"} 3])
  (pd [:osc- {:name "slow"} 1/10])
  (pd [:min- {:name "low"} (other "fast") (other "slow")])
  (pd [:max- {:name "high"} (other "fast") (other "slow")])
  (pd [://- {:name "tap"}
       [://- 2
        [:--
         [:+- (other "high") 1]
         [:+- (other "low") 1]]]
       (other "src")])
  (pd [:*- {:name "final"} 0.1
       [:clip- (other "tap") -1 1]])
  (pd [:dac- (other "final") (other "final")])
  )

(open-pd)
(load "clippercore.pd")
