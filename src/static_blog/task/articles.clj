(ns static-blog.task.articles
  ;;
  ;; Responsible for publishing the single-page permanent pages for
  ;; blog articles.
  ;;
  (:require
   [clojure.pprint :as pp]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [static-blog.task.task :as task]
   [static-blog.lib.utils :as utils]
   [static-blog.lib.site :as site]
   [static-blog.lib.posts :as posts]))

(deftype ArticlesTask []
  task/Task
  ;;
  (concern [this]
    "Articles Task")
  ;;
  (invoke! [this site]
    (println "\n" (task/concern this))
    (let [template (slurp (site/template site :post))]
      (doseq [post (posts/posts site)]
        (let [target (io/as-file (:post-target post))
              content (utils/md->html (:post-body post) :site-url (:site-url site))
              data (assoc (into site post) :post-text content)]
          (println " - publishing" target)
          (utils/mk-dir target)
          (spit target (utils/merge-template template data)))))))

(defn mk-task
  []
  (ArticlesTask.))
