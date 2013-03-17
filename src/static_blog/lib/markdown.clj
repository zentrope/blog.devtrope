(ns static-blog.lib.markdown
  ;;
  ;; Small module for processing markdown, mainly so that we can
  ;; use different implementations if available.
  ;;
  (:import
   [org.pegdown PegDownProcessor Extensions])
  ;;
  (:require
   [clojure.java.shell :as shell]
   [clojure.java.io :as io]
   [clojure.string :as string]))

(def ^:private sep java.io.File/separator)
(def ^:private mmd-bin "multimarkdown")
(def ^:private bin-path (System/getenv "PATH"))
(def ^:private classpath-sep (System/getProperty "path.separator"))

(defn- as-path
  [& elements]
  (string/join sep elements))

(defn- find-binary
  []
  (->> (string/split bin-path (re-pattern classpath-sep))
       (map string/trim)
       (map #(as-path % mmd-bin))
       (filter #(.exists (io/as-file %)))
       (first)))

(defmulti markdown-impl (fn [impl string] impl))

(defmethod markdown-impl :pegdown
  [_ string]
  (let [ext (- (Extensions/ALL) (Extensions/HARDWRAPS))
        processor (PegDownProcessor. ext)]
    (.markdownToHtml processor string)))

(defmethod markdown-impl :multimarkdown
  [_ string]
  (:out (shell/sh (find-binary) "--notes" "--smart" :in string)))

(defn as-html
  [string]
  (if-let [bin (find-binary)]
    (markdown-impl :multimarkdown string)
    (markdown-impl :pegdown string)))
