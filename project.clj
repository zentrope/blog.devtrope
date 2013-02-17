(defproject static-blog "0.1"
  :description "A static blog site maker-upper."
  :url "http://flippingthebozobit.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0-RC16"]
                 [org.clojure/tools.cli "0.2.2"]
                 [org.pegdown/pegdown "1.2.0"]]
  :main static-blog.core)
