(ns static-blog.lib.utils

  (:require
   [clojure.string :as string])

  (:import
   [org.pegdown PegDownProcessor Extensions]))

(def sep java.io.File/separator)

(defn path-from-vec
  "Turn a vector of strings into a file path."
  [& parts]
  (string/join sep parts))

(defn path-from-keys
  "Transform the values of the provided keys into a file path."
  [hash & keys]
  (let [reducer (fn [a k] (if-let [v (k hash)] (conj a v) a))
        vals (reduce reducer [] keys)]
    (string/join sep vals)))

(defn merge-template
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
  [raw & more]
  (let [data (into {} (apply hash-map more))
        md-extensions (- (Extensions/ALL) (Extensions/HARDWRAPS))
        processor (PegDownProcessor. md-extensions)]
    (merge-template (.markdownToHtml processor raw) data)))
