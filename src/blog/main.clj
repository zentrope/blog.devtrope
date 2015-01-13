(ns blog.main
  (:gen-class)
  (:refer-clojure :exclude [replace])
  (:require
    [clojure.edn :as edn :only [read-string]]
    [clojure.string :as string :refer [replace]]
    [clojure.java.io :as io]
    [clojure.java.shell :as shell]
    [clojure.data.xml :refer [indent-str sexp-as-element]]
    [hiccup.page :refer [html5 include-css]]))

;;-----------------------------------------------------------------------------

(def ^:private rfc822
  (java.text.SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss ZZZZ"))

(def ^:private mdate
  (java.text.SimpleDateFormat. "yyyy-MM-dd"))

(defn- ndate
  []
  (.format rfc822 (java.util.Date.)))

(defn- fdate
  [d]
  (->> (.parse mdate d)
       (.format rfc822)))

;;-----------------------------------------------------------------------------

(defn- delete-file-recursively!
  [f]
  (let [f (io/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively! child)))
    (io/delete-file f)))

(defn- copy-dir!
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

;;-----------------------------------------------------------------------------

(defn- container
  [& body]
  (html5
   [:head
    [:title "Devtrope"]
    [:meta {:charset "utf-8"}]
    [:meta {:http-quiv "X-UA-Compatible" :content "IE=edge"}]
    [:meta {:name "viewport" :content "width=device-width"}]
    [:link {:rel "alternate" :type "application/rss+xml" :title "RSS"
            :href "http://devtrope.com/feeds/rss.xml"}]
    [:link {:rel "shortcut icon" :href "favicon.ico"}]
    (include-css
     "http://fonts.googleapis.com/css?family=EB+Garamond&subset=latin,latin-ext")
    (include-css "/style.css")]
   [:body
    [:header
     [:div.title [:a {:href "/"} "Devtrope"]]
     [:div.author "Keith Irwin"]]
    [:section#container
     body]
    [:footer
     [:div.copyright "&copy; 2009-2014 Keith Irwin. All rights reserved."]]
    [:script {:type "text/javascript"}
"var _gauges = _gauges || [];
  (function() {
    var t   = document.createElement('script');
    t.type  = 'text/javascript';
    t.async = true;
    t.id    = 'gauges-tracker';
    t.setAttribute('data-site-id', '514634a4f5a1f57bbf0000da');
    t.src = '//secure.gaug.es/track.js';
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(t, s);
 })();"
     ]]))

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
    [:div.contents
     [:ul
      (for [{:keys [slug title when]} posts]
        [:li
         [:div.date when]
         " "
         [:span.link [:a {:href (str "post/" slug "/")} title]]])]]]
   (for [p pages]
     [:section.page
      [:h1 (:title p)]
      (:html p)])))

(defn- rss-feed
  [posts]
  (clojure.string/replace
   (indent-str
    (sexp-as-element
     [:rss {:version "2.0"}
      [:channel
       [:title "Devtrope"]
       [:description "Scintillating Observations."]
       [:link "http://devtrope.com"]
       [:lastBuildDate (ndate)]
       [:pubDate (ndate)]
       [:ttl 1800]
       (for [p (reverse (sort-by :when posts))]
         [:item
          [:title (:title p)]
          [:link (str "http://devtrope.com/post/" (:slug p) "/")]
          [:guid (str "http://devtrope.com/post/" (:slug p) "/")]
          [:pubDate (fdate (:when p))]
          [:description [:-cdata (:html p)]]
          ])]]))
   "><" ">\n<"))

;;-----------------------------------------------------------------------------

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

;;-----------------------------------------------------------------------------

(defn -main
  [& args]
  (println "Running.")

  (let [texts (load-index!)
        root (io/as-file "pub")
        posts (mapv #(assoc % :html (markdown! (:text %))) (scoop-posts texts))
        pages (mapv #(assoc % :html (markdown! (:text %))) (scoop-pages texts))]
    ;;
    (when (.exists root)
      (delete-file-recursively! root))
    ;;
    (.mkdirs root)
    ;;
    (spit (io/file root "index.html") (index-page posts pages))
    ;;
    (copy-dir! (io/file (io/resource "assets"))
               root)
    ;;
    (let [feed-dir (io/file root "feeds")]
      (.mkdirs feed-dir)
      (spit (io/file feed-dir "rss.xml")
            (rss-feed posts)))
    ;;
    (doseq [post posts]
      (let [html (post-page (:title post) (:when post) (:html post))
            loc (io/as-file (str "pub/post/" (:slug post)))
            place (io/file loc "index.html")]
        (.mkdirs loc)
        (println " - " (.getPath place))
        (spit place html))))

  (println "Done.")
  (System/exit 0))
