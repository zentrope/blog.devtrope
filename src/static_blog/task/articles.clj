(ns static-blog.task.articles
  ;;
  ;; Responsible for publishing the single-page permanent pages for
  ;; blog articles.
  ;;
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [static-blog.task.task :as task]
   [static-blog.lib.utils :as utils]
   [static-blog.lib.site :as site]
   [static-blog.lib.posts :as posts]))

(defn- mk-path-vector
  [site post]
  (-> (str (:post-url post)
           "/"
           (get-in site [:article-page :output-page]))
      (string/split #"/")))

(defn- target-path
  [site post]
  (->> (mk-path-vector site post)
       (filter (complement empty?))
       (apply utils/path-from-vec)
       (utils/path-from-vec (:target-dir site))
       (io/as-file)))

(defn- publish!
  [site template post]
  (let [target (target-path site post)
        content (utils/md->html (:post-body post) :site-url (:site-url site))
        data (assoc (into site post) :post-text content)]
    (println " - publishing" target)
    (utils/mk-dir target)
    (spit target (utils/merge-template template data))))

(defn- publish-posts!
  [site]
  (let [template (site/slurp-template site :article-page :main-template)]
    (doseq [post (posts/posts site)]
      (publish! site template post))))

(deftype ArticlesTask []
  task/Task
  ;;
  (concern [this]
    "Articles Task")
  ;;
  (invoke! [this site]
    (publish-posts! site)))

(defn mk-task
  []
  (ArticlesTask.))
