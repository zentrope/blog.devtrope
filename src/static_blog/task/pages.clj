(ns static-blog.task.pages
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [static-blog.task.task :as task]
   [static-blog.lib.utils :as utils]))

(def ^{:private true :dynamic true} *site* {})

(defn- page-dir
  []
  (utils/path-from-keys *site* :source-dir :page-dir))

(defn- template-dir
  []
  (utils/path-from-keys *site* :source-dir :template-dir))

(defn- template
  []
  (slurp (utils/path-from-vec (template-dir)
                              (get-in *site* [:static-page :main-template]))))

(defn- output-page
  []
  (get-in *site* [:static-page :output-page]))

(defn- page-title
  [^java.io.File file]
  (string/capitalize (utils/file-name file)))

(defn- page-target
  [file]
  (utils/path-from-vec (:target-dir *site*)
                       (utils/rel-parent file (page-dir))
                       (utils/file-name file)
                       (output-page)))

(defn- page-content
  [^java.io.File file]
  (-> file
      (utils/md->html)
      (utils/merge-template {:site-url (:site-url *site*)})))

(defn- build-page
  [^java.io.File file]
  {:site-url (:site-url *site*)
   :page-title (page-title file)
   :page-content (page-content file)
   :target (page-target file)})

(defn- pages
  []
  (->> (io/as-file (page-dir))
       (file-seq)
       (filter #(.isFile %))
       (map build-page)))

(defn- publish-pages!
  []
  (doseq [{:keys [page-title target page-content] :as page} (pages)]
    (let [out (io/as-file target)]
      (println " - writing" out)
      (.mkdirs (.getParentFile out))
      (spit out (utils/merge-template (template) page)))))

(deftype PagesTask []
  task/Task
  ;;
  (concern [this]
    "Pages Task")
  ;;
  (invoke! [this site]
    (binding [*site* site]
      (publish-pages!))))

(defn mk-task
  []
  (PagesTask.))
