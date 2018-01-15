(ns clj-puredata.comms
  "PureData OSC communication and live reloading of patches."
  (:require [overtone.osc :refer [osc-client
                                  osc-send]]
            [clojure.java.shell :refer [sh]]))

(defonce pd-osc-client (osc-client "localhost" 5000))

(defonce pd-process (atom nil))

(defonce reload-targets (atom []))

(defn open-pd []
  "Attempts to run PureData and open OSC channel for communication."
  (reset! pd-process (future (sh "pd" "resources/reload-patch.pd"))))

(defn- send-to-pd
  [target & strings]
  (assert (re-matches #"/.*" target) "Argument string TARGET requires leading slash (\"/\").")
  (assert (every? #(= (type %) java.lang.String) strings) "Argument STRINGS requires a list of strings (java.lang.String).")
  (apply osc-send pd-osc-client target strings))

(defn- reload-patch [patch-file-name]
  (let [file (clojure.java.io/file patch-file-name)
        file-name (.getName file)
        dir (.getPath (.getParentFile (.getAbsoluteFile file)))]
    (send-to-pd "/reload" file-name dir)))

(defn reload []
  "Reloads all patches registered with LOAD-PATCH."
  (doseq [p @reload-targets]
    (reload-patch p)))

(defn load-patch [& patch-names]
  "Registers the patches to be reloaded after _any_ WITH-PATCH is called."
  (reset! reload-targets patch-names)
  (reload))
