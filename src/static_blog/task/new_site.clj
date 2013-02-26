(ns static-blog.task.new-site
  ;;
  ;; Creates a new basic site to make it a bit easier to
  ;; start something new.
  ;;
  (:require
   [clojure.java.io :as io])
  ;;
  (:require
   [static-blog.lib.utils :as utils]
   [static-blog.task.task :as task]))

(defn- target
  [file from to]
  (let [fname (.getName file)
        rpath (.substring (utils/rel-parent file (utils/full-path from)) 1)]
    (io/as-file (utils/path-from-vec (utils/full-path to) rpath fname))))

(defn- copy
  [source from to]
  (let [target (target source from to)]
    (println " - writing" target)
    (.mkdirs (.getParentFile target))
    (io/copy source target)))

(deftype NewSiteTask [to]
  task/Task

  (concern [this]
    "New Site Task")

  (invoke! [this site]
    (let [from (io/as-file (io/resource "scaffold"))]
      (doseq [file (->> (file-seq from) (filter #(.isFile %)))]
        (copy file from to)))))

(defn mk-task
  [location]
  (NewSiteTask. location))
