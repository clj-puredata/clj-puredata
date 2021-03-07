(defproject clj-puredata "0.1.1-SNAPSHOT"
  :description "generate PureData patches."
  :url "https://github.com/pdikmann/clj-puredata"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [overtone/osc-clj "0.9.0"]
                 [potemkin "0.4.4"]]
  :main clj-puredata.comms
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-codox "0.10.3"]])
