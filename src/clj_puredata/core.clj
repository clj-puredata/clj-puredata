(ns clj-puredata.core
  (:require [overtone.osc :refer [osc-client
                                  osc-send]]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io])
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
        dir (.getAbsolutePath (.getParentFile file))]
    (send-to-pd "/reload" file-name dir)))

(comment
  (do
    (open-pd)
    (Thread/sleep 2000)
    (reload-patch "resources/hello-world.pd")))

(defn -main
  [& args]
  ;;(println "Hello, World!")
  (open-pd)
  (Thread/sleep 2000)
  (reload-patch "resources/hello-world.pd")) 
