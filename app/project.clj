(defproject dhex "0.1.0-SNAPSHOT"
  :description "Big things start small"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.10.773"]
                 [ring/ring-core "1.12.1"]
                 [ring-cors "0.1.13"]
                 [org.clojure/data.json "2.5.0"]
                 [ring/ring-jetty-adapter "1.12.1"]
                 [ring/ring-defaults "0.5.0"]
                 [ring/ring-json "0.5.0"]
                 [compojure "1.7.1"]
                 [clj-http "3.13.0"]
                 [org.clojure/tools.logging "1.3.0"]]

  :plugins [[lein-ring "0.12.6"]]

  :ring {:handler dhex.handler/handler
         :auto-refresh? true
         :auto-reload? true
         :port 4000}

  :source-paths ["src/main" "test"]

  :target-path "target/%s"
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.2"]]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
