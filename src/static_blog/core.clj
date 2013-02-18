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

(def ^:dynamic *source* (str cwd sep "site"))
(def ^:dynamic *target* (str cwd sep "pub"))
(def ^:dynamic *site-url* (str "file://" *target*))

(defn- parent-of
  [f]
  (let [path (.getParent f)]
    (.replace path *source* "")))

(defn- mk-post-url
  [parent]
  (str *site-url* parent sep (if (.startsWith *site-url* "file://") "index.html" "")))

(defn- num?
  [s]
  (try (number? (read-string s))
       (catch Throwable t
         false)))

(defn- date-str
  [[year month day]]
  (->> (format "%4s-%2s-%2s" year month day)
       (.parse sdf-in)
       (.format sdf-out)))

(defn- mk-date
  [f]
  (->> (string/split (str f) (re-pattern sep))
       (filter #(num? %))
       (take 3)
       (date-str)))

(defn- site-data
  []
  {:site-url *site-url*})

(defn- merge-template
  [text data]
  (reduce (fn [a [k v]] (string/replace a (re-pattern (str k)) (str v))) text data))

(defn- md->html
  [raw]
  (let [md-extensions (- (Extensions/ALL) (Extensions/HARDWRAPS))
        processor (PegDownProcessor. md-extensions)]
    (merge-template (.markdownToHtml processor raw) (site-data))))

(defn- build-story
  [file]
  {:site-url *site-url*
   :post-file file
   :post-date (mk-date file)
   :post-url (mk-post-url (parent-of file))
   :post-title (string/replace (.getName file) #"[.]md" "")
   :post-text (md->html (slurp file))
   :post-target (str *target* (parent-of file) sep "index.html")})

(defn- stories
  []
  (->> (io/as-file (str *source* sep "articles"))
       (file-seq)
       (sort)
       (reverse)
       (filter #(.isFile %))
       (map build-story)))

(defn- asset-files
  []
  (->> (io/as-file (str *source* sep "assets"))
       (file-seq)
       (map #(.getAbsolutePath %))
       (map #(into {} {:target (.replace % (str *source* sep "assets") *target*)
                       :source %}))))

;;-----------------------------------------------------------------------------

(defn- template-for
  [type]
  (case type
    :article "article.html"
    :index "index.html"
    :archive "archive.html"
    :post "post.html"
    :archive-link "archive-link.html"
    :else (throw (Exception. (str "Unknown template " type ".")))))

(defn- template-path
  [type]
  (str *source* sep "templates" sep (template-for type)))

(defn- load-template
  [type]
  (let [path (io/as-file (template-path type))]
    (when (not (.exists path))
      (throw (Exception. (str "Unable to find template: " type))))
    (slurp path)))

;;-----------------------------------------------------------------------------

(defn- publish-posts!
  []
  (doseq [f (stories)]
    (let [template (load-template :article)
          target (io/as-file (:post-target f))]
      (println " publishing" target)
      (.mkdirs (.getParentFile target))
      (spit target (merge-template template f)))))

;;-----------------------------------------------------------------------------

(defn- publish-assets!
  []
  (doseq [{:keys [source target]} (asset-files)]
    (let [from (io/as-file source)
          to (io/as-file target)]
      (println " publishing" to)
      (when (.isDirectory from)
        (.mkdirs to))
      (when (.isFile from)
        (.mkdirs (.getParentFile to))
        (if (.endsWith (.getName from) ".html")
          (spit to (merge-template (slurp from) (site-data)))
          (io/copy from to))))))

;;-----------------------------------------------------------------------------

(defn- archive-data
  [posts]
  (->> (for [p posts] (merge-template (load-template :archive-link) p))
       (string/join "\n")
       (assoc (site-data) :archive-list)))

(defn- publish-archive!
  []
  (let [template (load-template :archive)
        target (io/as-file (str *target* sep "archive.html"))
        data (archive-data (stories))]
    (println " writing" target)
    (spit target (merge-template template data))))

;;-----------------------------------------------------------------------------

(defn- home-data
  [posts]
  (->> (for [p posts] (merge-template (load-template :post) p))
       (string/join "\n\n")
       (assoc (site-data) :post-list)))

(defn- publish-home!
  []
  (let [template (load-template :index)
        target (io/as-file (str *target* sep "index.html"))
        data (home-data (stories))]
    (println " writing" target)
    (spit target (merge-template template data))))

;;-----------------------------------------------------------------------------

(defn- do-run
  []
  (println "\nLocations:")
  (println " source:" *source*)
  (println " target:" *target*)
  (println " subdir:" *site-url*)
  (println "\nAssets:")
  (publish-assets!)
  (println "\nPosts:")
  (publish-posts!)
  (println "\nGenerated pages:")
  (publish-home!)
  (publish-archive!)
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
