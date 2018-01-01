(ns clj-puredata.usage
  "Examples for usage of clj-puredata."
  (:require [clj-puredata.core :refer [open-pd
                                       load-patch
                                       with-patch
                                       pd
                                       inlet
                                       outlet
                                       other]]))
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
    (pd [:text "lol"]))

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
    (pd [:dac- (other "final") (other "final")]))

  (with-patch "mutual.pd"
    {:width 800 :height 200}
    (pd [:+ {:name 'add} (other 'f) 1])
    (pd [:print {:x 0 :y 100}
         [:float {:name 'f}
          [:t {:name 't}
           [:msg "bang"]]
          (other 'add)]
         (inlet 0 (outlet 1 (other 't)))]))

  (with-patch "in-out-lets.pd"
    (pd [:+ (inlet 1 [:- (outlet 1 [:moses 5])])]))

  (with-patch "view-height.pd"
    {:width 800 :height 200
     :graph-on-parent true
     :hide-object-name true
     :view-width 100 :view-height 100
     :view-margin-x 5 :view-margin-y 5}
    (pd [:text {:x 20 :y 20} "hello"])
    (pd [:/ 1 nil [:+ 1 2 3]]))

  (with-patch "include-another-patch.pd"
    {:width 600 :height 600}
    (pd ["view-height.pd" {:x 10 :y 10}])
    (pd [:text {:x 10 :y 120} "Thats all folks!"]))

  (with-patch "float-and-symbol-nodes.pd"
    (pd [:t {:name 't} "b b b b" [:loadbang]])
    (pd (apply conj [:print]
               (map (partial inlet 0)
                    [[:float 1 (outlet 3 (other 't))]
                     [:f 2 (outlet 2 (other 't))]
                     [:symbol "foo" (outlet 1 (other 't))]]))))

  (with-patch "atom-nodes.pd"
    (pd [:t {:name 't} "b b" [:loadbang]])
    (pd [:floatatom {:name 'f :send-symbol "foo"} 123 (outlet 1 (other 't))])
    (pd [:symbolatom {:name 's :send-symbol "bar"} "mysym" (outlet 0 (other 't))])
    (pd [:print
         (inlet 0 [:r "foo"])
         (inlet 0 [:r "bar"])])))

(with-patch "bng-and-tgl.pd"
  (pd [:tgl {:x 10 :y 10}])
  (pd [:bng {:x 30 :y 10}]))

(comment
  (open-pd)
  (load-patch "bng-and-tgl.pd")
  )

