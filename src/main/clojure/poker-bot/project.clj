(defproject poker-bot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [expectations "1.4.52"]]
  :plugins [[lein-autoexpect "1.2.2"]]
  :resource-paths ["resources/texas-holdem-java-client-1.1.20-jar-with-dependencies.jar"]
  :main poker-bot.core
  :aot [poker-bot.core])
