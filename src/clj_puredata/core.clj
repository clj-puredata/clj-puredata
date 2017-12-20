(ns clj-puredata.core
  (:require [overtone.osc :refer [osc-client
                                  osc-send]]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [clj-puredata.parse :refer [in-context pd]]
            [clj-puredata.translate :refer [translate-line]])
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

(def patch-defaults
  {:type :patch
   :x 0
   :y 0
   :width 450
   :height 300})

(defmacro with-patch [name options & rest]
  ;; TODO
  ;; - write to file with NAME
  ;; - use options like :graph-on-parent, :view-width, :view-height (hint: print at end of patch file)
  ;; - trigger reload (or write dedicated WITH-LIVE-PATCH for that).
  (let [forms (if (map? options)
                rest
                (conj rest options))]
    `(let [patch# (merge patch-defaults ~options)
           lines# (apply conj [patch#] (:lines (in-context ~@forms)))
           out# (map translate-line lines#)]
       out#)))

(defn -main
  [& args]
  (do (open-pd)
      (Thread/sleep 2000)
      (reload-patch "resources/hello-world.pd"))) 
