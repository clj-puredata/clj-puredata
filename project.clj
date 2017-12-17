(defproject clj-puredata "0.1.0-SNAPSHOT"
  :description "generate PureData patches."
  ;; :url "http://example.com/FIXME"
  ;; :license {:name "Eclipse Public License"
  ;;           :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [overtone/osc-clj "0.9.0"]]
  :main ^:skip-aot clj-puredata.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
