(ns static-blog.lib.site
  ;;
  ;; Convenience functions for extracting information out of a
  ;; site-description.
  ;;
  (:require
   [clojure.string :as string]
   [static-blog.lib.utils :as utils]))

(def sep java.io.File/separator)

(defn- file-name
  [^java.io.File file]
  (-> (.getName file)
      (string/replace #"[.][^.]+$" "")))

(defn- resolve-home
  [path]
  (if (.startsWith path "~")
    (str (System/getProperty "user.home") (.substring path 1))
    (str (System/getProperty "user.dir") sep path)))

(defn root-dir
  [site]
  (-> (get-in site [:source :root :dir])
      (resolve-home)))

(defn target-dir
  [site]
  (-> (get-in site [:target :root :dir])
      (resolve-home)))

(defn source-dir-in
  [site key]
  (str (root-dir site) "/" (get-in site [:source key :dir])))

(defn page-file-out
  [site file]
  (let [fname (.getName file)
        fpath (.replace (.getAbsolutePath file) (source-dir-in site :pages) (target-dir site))
        fout (get-in site [:source :templates :page :out])]
    (str (.replace fpath fname (file-name file)) sep fout)))

(defn post-file-out
  [site rel-url]
  (str (target-dir site)
       (string/join sep (string/split rel-url #"[/]"))
       sep
       (get-in site [:source :templates :post :out])))

(defn aggregate-file-out
  [site template]
  (str (target-dir site)
       sep
       (get-in site [:source :templates template :out])))

(defn- from-template
  [site template item]
  (str (source-dir-in site :templates)
       sep
       (get-in site [:source :templates template item])))

(defn template
  [site template]
  (from-template site template :main))

(defn sub-template
  [site template]
  (from-template site template :sub))
