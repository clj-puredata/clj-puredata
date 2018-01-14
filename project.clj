(defproject clj-puredata "0.1.0-SNAPSHOT"
  :description "generate PureData patches."
  :url "https://github.com/pdikmann/clj-puredata"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [overtone/osc-clj "0.9.0"]
                 [potemkin "0.4.4"]]
  :main ^:skip-aot clj-puredata.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-codox "0.10.3"]])
