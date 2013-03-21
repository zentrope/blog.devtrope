(ns static-blog.lib.posts
  ;;
  ;; General access to the list of posts so multiple tasks
  ;; can make use of them.
  ;;
  (:import
   [java.text SimpleDateFormat]
   [java.util Calendar])
  ;;
  ;;
  (:require
   [static-blog.lib.site :as site])
  ;;
  ;;
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]))

(def ^:private datef (SimpleDateFormat. "yyyy-MM-dd hh:mm"))
(def ^:private rfc822 (SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss ZZZZ"))
(def ^:private pretty (SimpleDateFormat. "EEEE, MMMM dd, yyyy"))
(def ^:private weekday (SimpleDateFormat. "EEEE"))
(def ^:private monthday (SimpleDateFormat. "MMMM"))

(defn- read-text
  "Reads in the raw markdown source, removing everything above the first blank line."
  [f]
  (let []
    (loop [headers []
           lines (string/split (slurp f) #"\n")]
      (if (empty? (string/trim (first lines)))
        [(string/join "\n" headers) (string/join "\n" (rest lines))]
        (recur (conj headers (first lines)) (next lines))))))

(defn- read-header
  [f]
  (try
    (let [[header body] (read-text f)]
      (assoc (read-string header)
        :post-header header
        :post-body body
        :post-source (.getAbsolutePath f)
        :post-file f))
    (catch Throwable t
      (println "  WARNING: [" f "], can't process: " t)
      {})))

(defn- sources
  [location]
  (->> (file-seq location)
       (filter #(.endsWith (.getName %) "md"))
       (filter #(.isFile %))
       (map read-header)
       (filter map?)))

(defn- mk-cal
  [date time]
  (doto (Calendar/getInstance)
    (.setTime (.parse datef (str date " " time)))))

(defn- pad
  [num]
  (format "%02d" num))

(defn- datetime-explode
  [cal]
  (assoc {}
    :post-feed-date (.format rfc822 (.getTime cal))
    :post-pretty-date (.format pretty (.getTime cal))
    :post-timestamp (.getTimeInMillis cal)
    :post-year (.get cal Calendar/YEAR)
    :post-month (pad (inc (.get cal Calendar/MONTH)))
    :post-day (pad (.get cal Calendar/DATE))
    :post-name-of-day (.format weekday (.getTime cal))
    :post-name-of-month (.format monthday (.getTime cal))
    :post-hour (pad (.get cal Calendar/HOUR_OF_DAY))
    :post-minute (pad (.get cal Calendar/MINUTE))))

(defn- assoc-time
  [articles]
  (for [a articles]
    (-> (merge a (datetime-explode (mk-cal (:post-date a) (:post-time a))))
        (dissoc :post-date)
        (dissoc :post-time))))

(defn- assoc-target
  [site posts]
  (for [p posts] (assoc p :post-target (site/post-file-out site (:post-url p)))))

(defn- assoc-permalink
  [site-url posts]
  (for [p posts]
    (let [{:keys [post-slug post-year post-day post-month]} p
          links [site-url "articles" post-year post-month post-day post-slug]]
      (assoc p
        :post-url (string/join "/" links)))))

;;-----------------------------------------------------------------------------
;; Public
;;-----------------------------------------------------------------------------

(defn posts
  "Returns a list of metadata hashmaps for each post."
  [site]
  (->> (sources (io/as-file (site/source-dir-in site :posts)))
       (assoc-time)
       (assoc-permalink (:site-url site))
       (assoc-target site)))
