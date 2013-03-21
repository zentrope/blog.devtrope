(ns static-blog.task.pages
  ;;
  ;; Publishes static pages (about, contact, etc).
  ;;
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [static-blog.task.task :as task]
   [static-blog.lib.site :as site]
   [static-blog.lib.utils :as utils]))

(defn- build [site file]
  {:source file
   :target (site/page-file-out site file)
   :page-content (-> (slurp file) (utils/md->html :site-url (:site-url site)))
   :page-title (string/capitalize (utils/file-name file))})

(defn- pages [site]
  (->> (io/as-file (site/source-dir-in site :pages))
       (file-seq)
       (filter #(.isFile %))
       (map (partial build site))))

(deftype PagesTask []
  task/Task
  ;;
  (concern [this]
    "Pages Task")
  ;;
  (invoke! [this site]
    (println "\n" (task/concern this))
    (let [template (slurp (site/template site :page))]
      (doseq [{:keys [page-title target page-content] :as page} (pages site)]
        (let [out (io/as-file target)]
          (println " - publishing" out)
          (.mkdirs (.getParentFile out))
          (spit out (utils/merge-template template (into site page))))))))

(defn mk-task
  []
  (PagesTask.))
