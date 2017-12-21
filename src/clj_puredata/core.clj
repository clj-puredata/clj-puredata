(ns clj-puredata.core
  (:require [overtone.osc :refer [osc-client
                                  osc-send]]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [clojure.string :as string]
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
        dir (.getPath (.getParentFile (.getAbsoluteFile file)))]
    (send-to-pd "/reload" file-name dir)))

(def patch-defaults
  {:type :patch
   :x 0
   :y 0
   :width 450
   :height 300})

(defn wrap-lines [patch lines]
  (let [{:keys [graph-on-parent subpatch]} patch
        header-keys (select-keys patch [:x :y :width :height])
        footer-keys (select-keys patch [:graph-on-parent :view-width :view-height])
        subpatch-footer-keys (select-keys patch [:subpatch :parent-x :parent-y :name])
        header (merge {:type :patch-header}
                      header-keys)
        footer-or-nil (and graph-on-parent
                           (merge {:type :patch-footer}
                                  footer-keys))
        subpatch-footer-or-nil (and subpatch
                                    (merge {:type :subpatch-footer}
                                           subpatch-footer-keys))]
    (remove nil? (cons header (conj lines footer-or-nil subpatch-footer-or-nil)))))

(defmacro with-patch [name options & rest]
  ;; TODO
  ;; - trigger reload (or write dedicated WITH-LIVE-PATCH for that).
  (let [[patch forms] (if (map? options)
                        [(merge patch-defaults options) rest]
                        [patch-defaults (cons options rest)])]
    `(let [lines# (wrap-lines ~patch (:lines (in-context ~@forms)))
           out# (map translate-line lines#)]
       out#)))

(defn write-patch [file lines]
  (spit file (string/join "\n" lines)))

(defn -main
  [& args]
  (do (open-pd)
      (Thread/sleep 2000)
      (reload-patch "resources/hello-world.pd"))) 
