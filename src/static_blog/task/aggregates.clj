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
   [static-blog.lib.content :as content]
   [static-blog.lib.utils :as utils]))

(defn- assemble
  [template articles]
  (->> (for [a articles] (utils/merge-template template a))
       (string/join "\n\n")))

(defn- publish-all!
  [site page]
  (let [template (site/slurp-template site page :main-template)
        sub-template (site/slurp-template site page :sub-template)
        articles (content/articles site)
        data (assoc site :article-list (assemble sub-template articles))
        target (io/as-file (site/output-page site page))]
    (.mkdirs (.getParentFile target))
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
