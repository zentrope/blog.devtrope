(defproject static-blog "0.1"
  :description "A static blog site maker-upper."
  :url "http://github.com/zentrope/zentrope-sb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/tools.cli "0.2.2"]
                 [org.pegdown/pegdown "1.2.1"]
                 [ch.qos.logback/logback-classic "1.0.9"]
                 [ring/ring-jetty-adapter "1.2.0-beta1"]
                 [digest "1.4.3"]]

  :main static-blog.main)
