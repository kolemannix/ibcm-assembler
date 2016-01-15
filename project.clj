(defproject ibass "0.1.0-SNAPSHOT"
  :description "ibass (Itty Bitty Assembler)"
  :url "http://github.com/kolemannix/ibcm-assembler"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [midje "1.8.3"]]
  :plugins [[lein-figwheel "0.5.0-1"]]
  :clean-targets [:target-path "out"]
  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main "ibass.core"}}]}
  :main ibass.core)
