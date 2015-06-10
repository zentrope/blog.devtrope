(defproject blog "3"
  :description "A static blog site maker-upper."
  :url "https://github.com/zentrope/blog.devtrope"
  :license {:name "EPL" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/data.xml "0.0.8"]
                 [hiccup "1.0.5"]]
  :clean-targets ["target" "pub"]
  :jvm-opts ["-Dapple.awt.UIElement=true"]
  :min-lein-version "2.4.2"
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-ancient "0.6.7"]]
  :main ^:skip-aot blog.main)
