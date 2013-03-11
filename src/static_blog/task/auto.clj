(ns static-blog.task.auto
  ;;
  ;; Watch and re-process the source documents in real(-ish) time.
  ;;
  (:require
   [digest :as digest]
   [clojure.java.io :as io])
  ;;
  (:require
   [static-blog.lib.utils :as utils]
   [static-blog.lib.site :as site]
   [static-blog.task.task :as task]
   [static-blog.task.assets :as assets]
   [static-blog.task.pages :as pages]
   [static-blog.task.articles :as articles]
   [static-blog.task.aggregates :as aggregates]))

(def ^:private last-match (atom {}))
(def ^:private this-match (atom {}))

(def ^:private tasks {:asset [(assets/mk-task)]
                      :page [(pages/mk-task)]
                      :article [(articles/mk-task)
                                (aggregates/mk-task "Archive Page Task" :archive-page)
                                (aggregates/mk-task "Home Page Task" :home-page)
                                (aggregates/mk-task "RSS Feed Task" :feed-page)]
                      :template [(articles/mk-task)
                                 (pages/mk-task)
                                 (aggregates/mk-task "Archive Page Task" :archive-page)
                                 (aggregates/mk-task "Home Page Task" :home-page)
                                 (aggregates/mk-task "RSS Feed Task" :feed-page)]})

(defn- regenerate
  [site event]
  (println " - detected" (name event) "change event, regenerating...")
  (doseq [t (event tasks)]
    (task/invoke! t site)))

(defn- event
  [site file]
  (let [fname (utils/full-path file)]
    (cond
      (.startsWith fname (site/posts-dir site)) :article
      (.startsWith fname (site/article-dir site)) :article
      (.startsWith fname (site/template-dir site)) :template
      (.startsWith fname (site/page-dir site)) :page
      (.startsWith fname (site/asset-dir site)) :asset
      :else :noop)))

(defn- stamp
  "Return enough unique information about a file to make an
   identifying hash."
  [file]
  (format "%s:%s:%s" (.getAbsolutePath file) (.length file) (.lastModified file)))

(defn- hash-file
  "Return a unique hash for the given file or directory."
  [file]
  (if (.isFile file)
    (digest/md5 (stamp file))
    (digest/md5 (utils/full-path file))))

(defn- build-file
  [file]
  {:hash (hash-file file)
   :file file})

(defn- file-reducer
  [accum {:keys [hash file]}]
  (assoc accum hash file))

(defn- source-files
  "Return a map of file hashes to files."
  [dir]
  (->> (file-seq (io/as-file dir))
       (filter #(not (re-find #"[.]git" (str %))))
       (map build-file)
       (reduce file-reducer {})))

(defn- find-difference
  "Return a map with the items in 'this' not found in 'last'."
  [last this]
  (reduce (fn [a [k v]] (if (get last k) a (assoc a k v))) {} this))

(defn- refresh-match!
  [source-dir]
  (reset! last-match @this-match)
  (reset! this-match (source-files source-dir)))

(defn- initial-match!
  [source-dir]
  (reset! last-match (source-files source-dir))
  (reset! this-match @last-match))

(defn- test-for-changes
  [site]
  (refresh-match! (:source-dir site))
  (let [diffs (merge(find-difference @last-match @this-match)
                    (find-difference @this-match @last-match))]
    ;;
    ;; Not super sophisticated. For instance, a template event regens
    ;; just about everything except assets, but we don't therefore
    ;; filter out redundant events. So :template followed by :page
    ;; will regen pages twice. *shrug* I don't think it matters that
    ;; much for a single user app.
    ;;
    (let [events (reduce (fn [a [x y]] (conj a (event site y))) #{} diffs)]
      (doseq [e events]
        (regenerate site e))))
  (Thread/sleep (or (get-in site [:watcher :wait-time]) 5000))
  (recur site))

(defn- start-watcher
  [site]
  ;;
  ;; Would be smarter to restart a new thread each time and catch/log
  ;; exceptions.
  ;;
  (doto (Thread. (fn [] (test-for-changes site)))
    (.setName "auto-watcher")
    (.start)))

(deftype AutoTask []
  task/Task
  (concern [this]
    "Auto Task")
  (invoke! [this site]
    (let [source-dir (:source-dir site)]
      (initial-match! source-dir)
      (println " - tracking files in" source-dir)
      (start-watcher site)
      (println " - auto-watcher started"))))

(defn mk-task
  []
  (AutoTask.))
