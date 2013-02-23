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
   [static-blog.lib.content :as content]))

(defn- target-path
  [site ^java.io.File file]
  (let [target (:target-dir site)
        source (:source-dir site)
        path (utils/rel-parent file source)
        fname (get-in site [:article-page :output-page])]
    (utils/path-from-vec target path fname)))

(defn- publish!
  [site template article]
  (let [target (io/as-file (target-path site (:article-file article)))]
    (println " - publishing" target)
    (.mkdirs (.getParentFile target))
    (spit target (utils/merge-template template article))))

(defn- publish-articles!
  [site]
  (let [template (site/slurp-template site :article-page :main-template)]
    (doseq [article (content/articles site)]
      (publish! site template article))))

(deftype ArticlesTask []
  task/Task
  ;;
  (concern [this]
    "Articles Task")
  ;;
  (invoke! [this site]
    (publish-articles! site)))

(defn mk-task
  []
  (ArticlesTask.))
