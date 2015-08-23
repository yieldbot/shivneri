(defproject yieldbot/shivneri "0.1.0-SNAPSHOT"
  :description "Read messages from kafka and inserts them in couchbase"
  :url "https://github.com/yieldbot/shivneri"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [yieldbot/torna "0.1.0-SNAPSHOT"]
                 [yieldbot/prismo "0.2.0-SNAPSHOT"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main ^:skip-aot shivneri.core
  :profiles {:uberjar {:aot :all}}
  :min-lein-version "2.5.0")
