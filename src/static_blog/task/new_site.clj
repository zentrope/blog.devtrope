(ns static-blog.task.new-site
  ;;
  ;; Creates a new basic site to make it a bit easier to
  ;; start something new.
  ;;
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string])
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

(defn- unpack-from-jar
  [to]
  (doseq [[fname stream] (utils/find-jar-entries #"scaffold")]
    (let [path (string/replace fname #"scaffold" "")
          target (io/as-file(utils/path-from-vec to path))]
      (println " - writing" target)
      (.mkdirs (.getParentFile target))
      (spit target (slurp stream)))))

(defn- unpack-from-fs
  [to]
  (let [from (io/as-file (io/resource "scaffold"))]
      (doseq [file (->> (file-seq from) (filter #(.isFile %)))]
        (copy file from to))))

(deftype NewSiteTask [to]
  task/Task

  (concern [this]
    "New Site Task")

  (invoke! [this site]
    (if (utils/running-in-jar?)
      (unpack-from-jar to)
      (unpack-from-fs to))))

(defn mk-task
  [location]
  (NewSiteTask. location))
