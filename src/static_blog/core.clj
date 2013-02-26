(ns static-blog.core
  (:gen-class)
  ;;
  (:require
   [static-blog.lib.utils :as utils]
   [static-blog.task.task :as task]
   [static-blog.task.assets :as assets]
   [static-blog.task.pages :as pages]
   [static-blog.task.articles :as articles]
   [static-blog.task.aggregates :as aggregates]
   [static-blog.task.serve :as serve]
   [static-blog.task.auto :as auto]
   [static-blog.task.new-site :as new-site])
  ;;
  (:require
   [clojure.tools.cli :as cli :only [cli]]
   [clojure.string :as string]
   [clojure.pprint :as pp]
   [clojure.java.io :as io]))

(def site {:site-url nil
           :source-dir nil
           :target-dir nil
           :publish-date nil
           :new-site-dir nil
           ;;
           :asset-dir "assets"
           :page-dir "pages"
           :template-dir "templates"
           :article-dir "articles"
           ;;
           :home-page {:main-template "home.html"
                       :sub-template "home-article.html"
                       :output-page "index.html"}
           ;;
           :archive-page {:main-template "archive.html"
                          :sub-template "archive-article.html"
                          :output-page "archive/index.html"}
           ;;
           :feed-page {:main-template "feed.rss"
                       :sub-template "feed-article.rss"
                       :output-page "feeds/rss.xml"}
           ;;
           :article-page {:main-template "article.html"
                          :output-page "index.html"}
           ;;
           :static-page {:main-template "page.html"
                         :output-page "index.html"}
           ;;
           :watcher {:wait-time 5000}
           :server {:port 4002}
           })

(def ^:private cwd (System/getProperty "user.dir"))
(def ^:private source (utils/path-from-vec cwd "site"))
(def ^:private target (utils/path-from-vec cwd "pub"))
(def ^:private site-url "")

(def ^:private tasks [(assets/mk-task)
                      (pages/mk-task)
                      (articles/mk-task)
                      (aggregates/mk-task "Archive Page Task" :archive-page)
                      (aggregates/mk-task "Home Page Task" :home-page)
                      (aggregates/mk-task "RSS Feed Task" :feed-page)])

(def ^:private servs [(auto/mk-task)
                      (serve/mk-task)])

(defn- run
  [site tasks]
  (doseq [t tasks]
    (println (str "\n" (task/concern t)))
    (task/invoke! t site)))

(defn- new-site
  [site location]
  (run site [(new-site/mk-task location)]))

(defn- do-run
  [site serve?]
  (println "\nLocations:")
  (println " - source:" (:source-dir site))
  (println " - target:" (:target-dir site))
  (println " - topurl:" (if (= ""  (:site-url site)) "/" (:site-url site)))

  (run site tasks)

  (when serve?
    (run site servs))

  (println "\nDone."))

(defn- parse
  [args]
  (cli/cli
   args
   ["-h" "--help" "Display this help message." :default false :flag true]
   ["-g" "--generate" "Generate the skeleton of a new site."]
   ["-s" "--source" "Location of your site's source files." :default source]
   ["-u" "--url" "URL representing the site's root." :default site-url]
   ["-t" "--target" "Place to put your generated site." :default target]))

(defn- configured
  [options]
  {:source-dir (utils/full-path (:source options))
   :docroot (utils/full-path (:target options))
   :target-dir (str (utils/full-path (:target options)) (:url options))
   :site-url (:url options)
   :publish-date (utils/publish-date)})

(defn -main
  "Application entry point."
  [& args]

  (println "\nHi ho! Hi ho!")

  (let [[options trailing usage] (parse args)]

    (when (:help options)
      (println usage)
      (System/exit 0))

    (when-let [location (:generate options)]
      (new-site {} location)
      (System/exit 0))

    (do-run (into site (configured options))
            (some #(= :serve %) (map keyword (map string/lower-case trailing))))))
