(ns blog.main
  (:gen-class)
  (:refer-clojure :exclude [replace])
  (:require
    [clojure.edn :as edn :only [read-string]]
    [clojure.string :as string :refer [replace]]
    [clojure.java.io :as io]
    [clojure.java.shell :as shell]
    [hiccup.page :refer [html5 include-css]]))

;; (def ^:private rfc822 (SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss ZZZZ"))

(defn delete-file-recursively!
  [f]
  (let [f (io/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively! child)))
    (io/delete-file f)))

(defn copy-dir!
  [from to]
  (let [[top & files] (file-seq from)
        root-path (str (.getPath top) "/")]
    (doseq [f files]
      (let [dest (io/file to (replace (.getPath f) root-path ""))]
        (.mkdirs (.getParentFile dest))
        (when (.isFile f)
          (println " - " (.getPath dest))
          (io/copy f dest))))))

(defn- markdown!
  [string]
  (:out (shell/sh "/usr/local/bin/mmd" "--notes" "--smart" :in string)))

(defn- container
  [& body]
  (html5
   [:head
    [:title "Devtrope"]
    [:meta {:charset "utf-8"}]
    [:meta {:http-quiv "X-UA-Compatible" :content "IE=edge"}]
    [:link {:rel "shortcut icon" :href "favicon.ico"}]
    (include-css "http://fonts.googleapis.com/css?family=EB+Garamond&subset=latin,latin-ext")
    (include-css "/style.css")]
   [:body
    [:header
     [:div.title [:a {:href "/"} "Devtrope"]]
     [:div.author "Keith Irwin"]]
    [:section#container
     body]
    [:footer
     [:div.copyright "&copy; 2009-2014 Keith Irwin. All rights reserved."]]]))

(defn- post-page
  [title date text]
  (container
   [:article
    [:h1 title]
    [:section.date date]
    [:section.body text]]))

(defn- index-page
  [posts pages]
  (container
   [:section.posts
    [:h1 "Contents"]
    [:ul
     (for [{:keys [slug title when]} posts]
       [:li
        [:span.date when]
        " "
        [:span.link [:a {:href (str "post/" slug "/")} title]]])]]
   (for [p pages]
     [:section.page
      [:h1 (:title p)]
      (:html p)])))

(defn- load-text!
  [f]
  (loop [headers []
         lines (string/split (slurp f) #"\n")]
    (if (empty? (string/trim (first lines)))
      (into (read-string (string/join " " headers))
            {:text (string/join "\n" (rest lines))})
      (recur (conj headers (first lines)) (next lines)))))

(defn- resource-file-seq
  [place]
  (->> (file-seq (io/as-file (io/resource place)))
       (filter (fn [f] (.isFile f)))))

(defn- load-index!
  []
  (->> (resource-file-seq "posts")
       (mapv load-text!)))

(defn- scoop-pages
  [index]
  (let [pages (filter #(= (:type %) :page) index)]
    (map #(select-keys % [:title :text]) pages)))

(defn- scoop-posts
  [texts]
  (->> (filter #(= (:type %) :post) texts)
       (sort-by :when)
       (reverse)))

(defn -main
  [& args]
  (println "Running.")

  (let [texts (load-index!)
        posts (scoop-posts texts)
        root (io/as-file "pub")
        pages (mapv #(assoc % :html (markdown! (:text %))) (scoop-pages texts))]
    ;;
    (when (.exists root)
      (delete-file-recursively! root))
    ;;
    (.mkdirs root)
    ;;
    (spit (io/file root  "index.html") (index-page posts pages))
    ;;
    (copy-dir! (io/file (io/resource "assets"))
               root)
    ;;
    (doseq [post posts]
      (let [html (post-page (:title post) (:when post) (markdown! (:text post)))
            loc (io/as-file (str "pub/post/" (:slug post)))
            place (io/file loc "index.html")]
        (.mkdirs loc)
        (println " - " (.getPath place))
        (spit place html))))

  (println "Done.")
  (System/exit 0))
