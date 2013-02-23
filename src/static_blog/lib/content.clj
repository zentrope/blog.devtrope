(ns static-blog.lib.content
  ;;
  ;; So that multiple tasks can get at the blog content.
  ;;
  (:import
   [java.text SimpleDateFormat])
  ;;
  (:require
   [static-blog.lib.utils :as utils]
   [static-blog.lib.site :as site]
   [clojure.string :as string]
   [clojure.java.io :as io]))

(def ^:private sdf-in (SimpleDateFormat. "yyyy-MM-dd"))
(def ^:private rfc822 (SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss ZZZZ"))
(def ^:private pretty (SimpleDateFormat. "EEEE, MMMM dd, yyyy"))

(defn- date-from-file
  "Return a date based on the one implied in an article path (by
   convention)."
  [^java.io.File file]
  (->> (string/split (str file) (re-pattern java.io.File/separator))
       (filter #(utils/num? %))
       (take 3)
       (apply format "%4s-%2s-%2s")
       (.parse sdf-in)))

(defn- rfc822-date-from-file
  "Returns RSS2 compatible date string."
  [^java.io.File file]
  (.format rfc822 (date-from-file file)))

(defn- pretty-date-from-file
  "Returns hard-codes date string (for now)."
  [^java.io.File file]
  (.format pretty (date-from-file file)))

(defn- permalink
  "Construct an article permalink."
  [^java.io.File file url source-dir]
  (let [path (utils/rel-parent file source-dir)]
    (if (= "" url)
      (utils/append-sep path)
      (utils/append-sep (utils/path-from-vec url path)))))

(defn- mk-article
  [site file]
  (let [source-dir (:source-dir site)
        url (:site-url site)
        path (utils/rel-parent file source-dir)
        permalink (permalink file url source-dir)]
    {:site-url url
     :article-file file
     :article-machine-date (rfc822-date-from-file file)
     :article-date (pretty-date-from-file file)
     :article-url permalink
     :article-title (utils/file-name file)
     :article-text (utils/md->html (slurp file)
                                   :article-url permalink
                                   :site-url url)}))

(defn articles
  [site]
  (->> (io/as-file (site/article-dir site))
       (file-seq)
       (filter #(.isFile %))
       (reverse)
       (map (partial mk-article site))))
