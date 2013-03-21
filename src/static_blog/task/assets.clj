(ns static-blog.task.assets
  ;;
  ;; Publishes the contents of the "assets" directory in the source to
  ;; the target.
  ;;
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [static-blog.task.task :as task]
   [static-blog.lib.site :as site]
   [static-blog.lib.utils :as utils]))

(defn- assets
  [from to]
  (->> (io/as-file from)
       (file-seq)
       (map #(.getAbsolutePath %))
       (map #(into {} {:target (.replace % from to) :source %}))))

(defn- htmlish?
  [f]
  (let [name (string/lower-case (.getName f))]
    (or (.endsWith name "html")
        (.endsWith name "css")
        (.endsWith name "js"))))

(defn- publish!
  [site from to]
  (when (.isDirectory from)
    (.mkdirs to))
  (when (.isFile from)
    (.mkdirs (.getParentFile to))
    (if (htmlish? from)
      (spit to (utils/merge-template (slurp from) site))
      (io/copy from to))))

(deftype AssetTask []
  task/Task

  (concern [this]
    "Asset Task")

  (invoke! [this site]
    (println "\n" (task/concern this))
    (let [source (site/source-dir-in site :assets)
          target (site/target-dir site)
          assets (assets source target)]
      (doseq [{:keys [source target]} assets]
        (let [from (io/as-file source)
              to (io/as-file target)]
          (println " - publishing" to)
          (publish! site from to))))))

(defn mk-task
  []
  (AssetTask.))
