(ns clj-puredata.core
  (:require [overtone.osc :refer [osc-client
                                  osc-send]]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clj-puredata.parse :refer [in-context
                                        pd
                                        inlet
                                        outlet]]
            [clj-puredata.translate :refer [with-patch]] 
  (:gen-class))

(defonce pd-osc-client (osc-client "localhost" 5000))
(def pd-process (atom nil))

(defn send-to-pd
  [target & strings]
  (assert (re-matches #"/.*" target) "Argument string TARGET requires leading slash (\"/\").")
  (assert (every? #(= (type %) java.lang.String) strings) "Argument STRINGS requires a list of strings (java.lang.String).")
  (apply osc-send pd-osc-client target strings))

(defn open-pd []
  (reset! pd-process (future (sh "pd" "resources/reload-patch.pd"))))

(defn reload-patch [patch-file-name]
  (let [file (clojure.java.io/file patch-file-name)
        file-name (.getName file)
        dir (.getPath (.getParentFile (.getAbsoluteFile file)))]
    (send-to-pd "/reload" file-name dir)))

(comment
  (with-patch "wobble.pd"
    {:width 800 :height 200}
    (let [gapper (fn [freq] (pd ["clip~" 0 1 ["*~" 20 ["osc~" freq]]]))
          out (pd ["*~" 0.1
                   ["+~"
                    (inlet (pd ["*~" (gapper 3) ["osc~" 500]]) 0)
                    (inlet (pd ["*~" (gapper 5) ["osc~" 400]]) 0)
                    (inlet (pd ["*~" (gapper 7) ["osc~" 300]]) 0)
                    (inlet (pd ["*~" (gapper 9) ["osc~" 200]]) 0)]])]
      (pd ["dac~" out out])))
  (reload-patch "wobble.pd")
  (with-patch "ref.pd"
    {:width 800 :height 200}
    (pd [:+ {:name 0} 1 2])
    (pd [:+ (other 0) (other 0)]))
  (reload-patch "ref.pd")
  (in-context (pd [:+ {:name 0} 1 2])
              (pd [:+ (other 0) (other 0)]))
  )

(defn -main
  [& args]
  (do (open-pd)
      (Thread/sleep 2000)
      (reload-patch "resources/hello-world.pd"))) 
