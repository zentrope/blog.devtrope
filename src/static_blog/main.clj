(ns static-blog.main
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
   [clojure.edn :as edn :only [read-string]]
   [clojure.string :as string]
   [clojure.java.io :as io]))

(defn- tasks
  []
  [(assets/mk-task)
   (pages/mk-task)
   (articles/mk-task)
   (aggregates/mk-task "Catalog Page Task" :catalog)
   (aggregates/mk-task "Home Page Task" :home)
   (aggregates/mk-task "RSS Feed Task" :feed)])

(defn- serve?
  [args]
  (not (empty? (filter #(= % "serve") args))))

(defn- acquire-site-info
  []
  (let [[_ name domain & config] (edn/read-string (slurp "site.clj"))]
    (-> (apply hash-map config)
        (assoc :site-http domain
               :site-name name
               :site-url ""
               :publish-date (utils/publish-date)))))

(defn -main
  [& args]
  (println ":: the-static-blog ::")
  (try
    (let [site (acquire-site-info)]
     (doseq [t (tasks)]
       (task/invoke! t site))
     (when (serve? args)
       (task/invoke! (auto/mk-task) site)
       (task/invoke! (serve/mk-task) site)))
    (catch Throwable t
      (println "\nERROR:" t))
    (finally
      (println "\nDone.")
      (System/exit 0))))
