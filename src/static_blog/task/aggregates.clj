(ns static-blog.task.aggregates
  ;;
  ;; For generating pages with articles mixed in (rss, home, archive, etc).
  ;;
  (:require
   [clojure.string :as string]
   [clojure.java.io :as io])
  ;;
  ;;
  (:require
   [static-blog.task.task :as task]
   [static-blog.lib.site :as site]
   [static-blog.lib.posts :as posts]
   [static-blog.lib.utils :as utils]))

(defn- with-markdown
  [site post]
  (assoc post :post-text (utils/md->html (:post-body post) :site-url (:site-url site))))

(defn- assemble
  [template site posts]
  (->> (for [p posts]
         (utils/merge-template template (with-markdown site p)))
       (string/join "\n\n")))

(defn- publish-all!
  [site page]
  (let [template (site/slurp-template site page :main-template)
        sub-template (site/slurp-template site page :sub-template)
        posts (reverse (sort-by :post-timestamp (posts/posts site)))
        data (assoc site :post-list (assemble sub-template site posts))
        target (io/as-file (site/output-page site page))]
    (utils/mk-dir target)
    (println " - publishing" target)
    (spit target (utils/merge-template template data))))

(deftype AggregatesTask [desc page]
  task/Task
  (concern [this]
    desc)
  (invoke! [this site]
    (publish-all! site page)))

(defn mk-task
  [desc page]
  (AggregatesTask. desc page))
