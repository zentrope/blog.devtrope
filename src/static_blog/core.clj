(ns static-blog.core
  (:gen-class)

  (:import
   [org.pegdown PegDownProcessor Extensions]
   [java.text SimpleDateFormat])

  (:require
   [clojure.tools.cli :as cli :only [cli]]
   [clojure.string :as string]
   [clojure.pprint :as pp]
   [clojure.java.io :as io]))

(def cwd (System/getProperty "user.dir"))
(def sep java.io.File/separator)
(def sdf-in (SimpleDateFormat. "yyyy-MM-dd"))
(def sdf-out (SimpleDateFormat. "EEEE, MMMM dd, yyyy"))
(def feed-out (SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss ZZZZ"))

(def publish-date (.format feed-out (System/currentTimeMillis)))

(def ^:dynamic *source* (str cwd sep "site"))
(def ^:dynamic *target* (str cwd sep "pub"))
(def ^:dynamic *site-url* (str "file://" *target*))

(defn- parent-of
  ([f old-path]
     (-> (.getParent f)
         (.replace old-path "")))
  ([f]
     (parent-of f *source*)))

(defn- num?
  [s]
  (try (number? (read-string s))
       (catch Throwable t
         false)))

(defn- mk-raw-date
  [f]
  (->> (string/split (str f) (re-pattern sep))
       (filter #(num? %))
       (take 3)
       (apply format "%4s-%2s-%2s")
       (.parse sdf-in)))

(defn- site-data
  []
  {:site-url *site-url*
   :publish-machine-date publish-date})

(defn- merge-template
  [text data]
  (reduce (fn [a [k v]] (string/replace a (re-pattern (str k)) (str v))) text data))

(defn- md->html
  [raw & more]
  (let [data (into (site-data) (apply hash-map more))
        md-extensions (- (Extensions/ALL) (Extensions/HARDWRAPS))
        processor (PegDownProcessor. md-extensions)]
    (merge-template (.markdownToHtml processor raw) data)))

(defn- file-name
  [file]
  (-> (.getName file)
      (string/replace #"[.][^.]+$" "")))

(defn- mk-article
  [file]
  (let [permalink (str *site-url* (parent-of file) sep)]
    {:site-url *site-url*
     :article-file file
     :article-machine-date (->> (mk-raw-date file) (.format feed-out))
     :article-date (->> (mk-raw-date file) (.format sdf-out))
     :article-url permalink
     :article-title (file-name file)
     :article-text (md->html (slurp file) :article-url permalink)
     :article-target (str *target* (parent-of file) sep "index.html")}b))

(defn- articles
  []
  (->> (io/as-file (str *source* sep "articles"))
       (file-seq)
       (reverse)
       (filter #(.isFile %))
       (map mk-article)))

(defn- assets
  []
  (->> (io/as-file (str *source* sep "assets"))
       (file-seq)
       (map #(.getAbsolutePath %))
       (map #(into {} {:target (.replace % (str *source* sep "assets") *target*)
                       :source %}))))

(defn- pages
  []
  (->> (io/as-file (str *source* sep "pages"))
       (file-seq)
       (filter #(.isFile %))
       (map #(into (site-data)
                   {:page-title (string/capitalize (file-name %))
                    :page-content (md->html (slurp %))
                    :target (str *target* sep (parent-of % (str *source* sep "pages"))
                                 sep (file-name %) sep "index.html")}))))

;;-----------------------------------------------------------------------------

(def template-in  {:home "home.html"
                   :home-article "home-article.html"
                   :archive "archive.html"
                   :archive-article "archive-article.html"
                   :feed "feed.rss"
                   :feed-article "feed-article.rss"
                   :article "article.html"
                   :page "page.html"})

(def template-out {:home "index.html"
                   :archive (str "archive" sep "index.html")
                   :feed "feed.rss"})

(defn- template-target
  [type]
  (if-let [place (get template-out type)]
    (io/as-file (str *target* sep place))
    (throw (Exception. (str "Unknown template target " type ".")))))

(defn- template-path
  [type]
  (str *source* sep "templates" sep (get template-in type)))

(defn- load-template
  [type]
  (let [path (io/as-file (template-path type))]
    (when (not (.exists path))
      (throw (Exception. (str "Unable to find template: " type))))
    (slurp path)))

;;-----------------------------------------------------------------------------

(defn- publish-articles!
  []
  (doseq [f (articles)]
    (let [template (load-template :article)
          target (io/as-file (:article-target f))]
      (println " publishing" target)
      (.mkdirs (.getParentFile target))
      (spit target (merge-template template f)))))

(defn- publish-pages!
  []
  (doseq [{:keys [page-title target page-content] :as page} (pages)]
    (let [out (io/as-file target)]
      (println " writing" out)
      (.mkdirs (.getParentFile out))
      (spit out (merge-template (load-template :page) page)))))

;;-----------------------------------------------------------------------------

(defn- htmlish?
  [f]
  (let [name (string/lower-case (.getName f))]
    (or (.endsWith name "html")
        (.endsWith name "css")
        (.endsWith name "js"))))

(defn- publish-assets!
  []
  (doseq [{:keys [source target]} (assets)]
    (let [from (io/as-file source)
          to (io/as-file target)]
      (println " publishing" to)
      (when (.isDirectory from)
        (.mkdirs to))
      (when (.isFile from)
        (.mkdirs (.getParentFile to))
        (if (htmlish? from)
          (spit to (merge-template (slurp from) (site-data)))
          (io/copy from to))))))

;;-----------------------------------------------------------------------------

(defn- article-data
  [template-key articles]
  (->> (for [a articles] (merge-template (load-template template-key) a))
       (string/join "\n\n")
       (assoc (site-data) :article-list)))

(defn- publish!
  [page-template article-template]
  (let [template (load-template page-template)
        target (template-target page-template)
        data (article-data article-template (articles))]
    (println " writing" target)
    (.mkdirs (.getParentFile target))
    (spit target (merge-template template data))))

(defn- publish-generated!
  []
  (let [generators [[:archive :archive-article]
                    [:home :home-article]
                    [:feed :feed-article]]]
        (doseq [g generators]
          (apply publish! g))))

;;-----------------------------------------------------------------------------

(defn- do-run
  []
  (println "\nLocations:")
  (println " source:" *source*)
  (println " target:" *target*)
  (println " subdir:" *site-url*)
  (println "\nAssets:")
  (publish-assets!)
  (println "\nPages:")
  (publish-pages!)
  (println "\nArticles:")
  (publish-articles!)
  (println "\nGenerated pages:")
  (publish-generated!)
  (println "\ndone."))

(defn- parse
  [args]
  (cli/cli
   args
   ["-h" "--help" "Display this help message." :default false :flag true]
   ["-s" "--source" "Location of your site's source files." :default *source*]
   ["-u" "--url" "URL representing the site'd root." :default *site-url*]
   ["-t" "--target" "Place to put your generated site." :default *target*]))

(defn -main
  "Application entry point."
  [& args]
  (let [[options trailing usage] (parse args)]

    (when (:help options)
      (println usage)
      (System/exit 0))

    (binding [*source* (.getAbsolutePath (io/as-file (:source options)))
              *target* (.getAbsolutePath (io/as-file (:target options)))
              *site-url* (if (:url options) (:url options) *site-url*)]
      (do-run))))
