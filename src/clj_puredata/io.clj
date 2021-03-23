(ns clj-puredata.io
  "PureData OSC communication and live reloading of patches."
  (:gen-class)
  (:require [overtone.osc :refer [osc-client
                                  osc-send]]
            [clojure.string :as string]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]))

(def pd-osc-client (atom nil))

(defonce pd-process (atom nil))

(defonce reload-targets (atom #{}))

(defonce dir-helpers "patches/helpers")

(defonce dir-target "patches")

(defn- copy-patches-from-uberjar
  []
  (.mkdirs (io/file dir-helpers))
  (doseq [patch ["reload-patch.pd"
                 "osc-to-pd.pd"
                 "oscparse-numbers.pd"
                 "for-each.pd"
                 "symbol-to-float.pd"]
          :let [in (io/input-stream (io/resource patch))
                out (io/file (str dir-helpers \/ patch))]]
    (io/copy in out)))

(defn open-pd
  "Attempts to run PureData and open OSC channel for communication."
  []
  (when-not (.exists (io/file (str dir-helpers \/ "reload-patch.pd")))
    (copy-patches-from-uberjar))
  (reset! pd-osc-client (osc-client "localhost" 5000))
  (reset! pd-process (future (sh "pd" "-path" dir-helpers "-path" dir-target "reload-patch.pd"))))

(defn- send-to-pd
  [target & strings]
  (assert (re-matches #"/.*" target) "Argument string TARGET requires leading slash (\"/\").")
  (assert (every? #(= (type %) java.lang.String) strings) "Argument STRINGS requires a list of strings (java.lang.String).")
  (try (apply osc-send @pd-osc-client target strings)
       (catch Exception e (str "Unable to send osc message to pd:" (.getMessage e)))))

(defn reload-patch [patch-file-name]
  (let [file (clojure.java.io/file (str dir-target \/ patch-file-name))
        file-name (.getName file)
        dir (.getPath (.getParentFile (.getAbsoluteFile file)))]
    (send-to-pd "/reload" file-name dir)))

(defn reload-all-patches
  "Reloads all patches registered with `load-patches`."
  []
  (doseq [p @reload-targets]
    (reload-patch p)))

(defn load-patches
  "Registers the patches to be reloaded when `write-patch-reload` or `reload-all-patches` is called."
  [& filenames]
  (reset! reload-targets filenames)
  (reload-all-patches))

(defn startup
  [& filenames]
  (open-pd)
  (when-not (empty? filenames)
    (Thread/sleep 3000)
    (apply load-patches filenames)))

(defn write
  [name patch]
  (.mkdirs (io/file dir-target))
  (let [output (string/join "\n" patch)]
    (spit (str dir-target \/ name) output)
    output))

(defn -main
  [& args]
  (open-pd))
