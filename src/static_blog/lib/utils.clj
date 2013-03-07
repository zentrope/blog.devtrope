(ns static-blog.lib.utils
  ;;
  (:require
   [clojure.string :as string]
   [clojure.java.io :as io])
  ;;
  (:require
   [static-blog.lib.markdown :as markdown])
  ;;
  (:import
   [java.text SimpleDateFormat]))

(def ^:private sep java.io.File/separator)
(def ^:private rfc822 (SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss ZZZZ"))

(defn full-path
  [file]
  (.getAbsolutePath (io/as-file file)))

(defn publish-date
  []
  (.format rfc822 (System/currentTimeMillis)))

(defn num?
  [s]
  (try (Long/parseLong s)
       (catch Throwable t
         false)))

;; TODO: redo this so that we can add a "/" to the end, optionally.
(defn path-from-vec
  "Turn a vector of strings into a file path."
  [& parts]
  (string/join sep parts))

;; TODO: get rid of this when path-from-vec is fixed.
(defn append-sep
  [path]
  (str path sep))

(defn path-from-keys
  "Transform the values of the provided keys into a file path."
  [hash & keys]
  (let [reducer (fn [a k] (if-let [v (k hash)] (conj a v) a))
        vals (reduce reducer [] keys)]
    (string/join sep vals)))

(defn merge-template
  "Replaces occurances of the keys in 'data' found in 'text' the the
   corresponding values found in 'data'."
  [text data]
  (reduce (fn [a [k v]] (string/replace a (re-pattern (str k)) (str v))) text data))

(defn file-name
  "Return the name of a file with path and extension removed."
  [^java.io.File file]
  (-> (.getName file)
      (string/replace #"[.][^.]+$" "")))

(defn rel-parent
  "Return the parent path of the file with the root path removed."
  [^java.io.File file ^String root]
  (-> (.getParent file)
      (.replace root "")))

(defn md->html
  "Convert markdown source to HTML, merging in template data if provided."
  [file & more]
  (let [data (into {} (apply hash-map more))]
    (merge-template (markdown/as-html file) data)))

(defn- find-this-class
  "Return the class of the namespace this function resides in."
  []
  (->> (str (namespace ::file-name) "__init")
       (namespace-munge)
       (symbol)
       (resolve)))

(defn- find-jar-file
  []
  (->> (.. (find-this-class)
           (getProtectionDomain)
           (getCodeSource)
           (getLocation)
           (toURI))
       (java.io.File.)
       (java.util.jar.JarFile.)))

(defn running-in-jar?
  "Are we running in a jar?"
  []
  (try (do (find-jar-file) true)
       (catch Throwable t false)))

(defn find-jar-entries
  "Find the jar we're running in, then return a tuple with the first
   term as the relative path to the file, and the second as an input
   stream to the file. Entries are filtered by names matching the
   regular expression."
  [re]
  (let [jar (find-jar-file)]
    (->> jar
         (.entries)
         (enumeration-seq)
         (filter #(re-find re (.getName %)))
         (map #(vector (.getName %) (.getInputStream jar %))))))
