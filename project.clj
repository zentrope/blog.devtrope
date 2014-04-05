(defproject blog "3"
  :description "A static blog site maker-upper."
  :url "https://github.com/zentrope/zentrope-sb"
  :license {:name "EPL" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [hiccup "1.0.5"]]
  :clean-targets ["target" "pub"]
  :jvm-opts ["-Dapple.awt.UIElement=true"]
  :min-lein-version "2.3.4"
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :main ^:skip-aot blog.main)
