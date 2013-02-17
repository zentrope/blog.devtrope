(ns static-blog.core
  ;;
  ;; Having just scrapped this together, I now see that this process
  ;; could really benefit from the same sort of "middleware" concept
  ;; the ring library uses as well as the notion of a "project" map,
  ;; such as leiningen uses.
  ;;
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
  (str *site-url* sep parent sep "index.html"))

(defn- num?
  [s]
  (try
    (number? (read-string s))
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

(defn- post-files
  []
  (->> (io/as-file (str *source* sep "articles"))
       (file-seq)
       (filter #(.isFile %))
       (map #(into {} {:file % :date (mk-date %) :location (mk-post-url (parent-of %))}))
       (sort-by :file)
       (reverse)))

(defn- source-files
  []
  (->> (io/as-file *source*)
       (file-seq)
       (filter #(-> (.getName %) (.endsWith ".md")))
       (map #(into {} {:target (str *target* (parent-of %) sep "index.html")
                       :date (mk-date %)
                       :src %}))))

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
    :else (throw (Exception. (str "Unknown template " type ".")))))

(defn- template-path
  [type]
  (str *source* sep "templates" sep (template-for type)))

(defn load-template
  [type]
  (let [path (io/as-file (template-path type))]
    (when (not (.exists path))
      (throw (Exception. (str "Unable to find template: " type))))
    (slurp path)))


(defn- md->title
  [raw]
  (let [lines (string/split raw #"\n")
        headings (filter #(.startsWith % "#") lines)
        heading (first headings)]
    (string/replace heading #"^[#]\s+" "")))

(def md-extensions (- (Extensions/ALL) (Extensions/HARDWRAPS)))

(defn- md->html
  [raw]
  (let [processor (PegDownProcessor. md-extensions)]
    (.markdownToHtml processor raw)))

;;-----------------------------------------------------------------------------
;; Articles

(defn- article-merge
  [template title date body]
  (-> (string/replace template #":site-url" *site-url*)
      (string/replace #":article-title" title)
      (string/replace #":date" date)
      (string/replace #":article" body)))

(defn- publish-article!
  [article]
  (let [template (load-template :article)
        raw (slurp (:src article))
        title (md->title raw)
        body (md->html raw)
        target (io/as-file (:target article))]
    (println " publishing" target)
    (.mkdirs (.getParentFile target))
    (spit target (article-merge template title (:date article) body))))

(defn- publish-articles!
  []
  (doseq [f (source-files)]
    (publish-article! f)))

;;-----------------------------------------------------------------------------
;; Assets

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
          (spit to (string/replace (slurp from) #":site-url" *site-url*))
          (io/copy from to))))))

;;-----------------------------------------------------------------------------
;; Home page

(defn- merge-index
  [template posts]
  (-> (string/replace template #":site-url" *site-url*)
      (string/replace #":articles" (string/join "\n\n" posts))))

(defn- merge-post
  [template title body date link]
  (-> (string/replace template #":site-url" *site-url*)
      (string/replace #":post-title" title)
      (string/replace #":post-body" body)
      (string/replace #":post-date" date)
      (string/replace #":permalink" link)))

(defn- slurp-special
  [file]
  (let [text (slurp file)
        title (md->title text)
        lines (string/split text #"\n")
        headless (filter #(not (re-matches #"^[#] .*$" %)) lines)]
    [title (string/join "\n" headless)]))

(defn- publish-home!
  []
  (let [template (load-template :post)
        index-template (load-template :index)
        index-file (io/as-file (str *target* sep "index.html"))
        posts (for [p (post-files)]
                (let [[title text] (slurp-special (:file p))]
                  (merge-post template
                              title
                              (md->html text)
                              (:date p)
                              (:location p))))]
    (println " writing" index-file)
    (spit index-file
          (merge-index index-template posts))))

;;-----------------------------------------------------------------------------

(defn- do-run
  []
  (println "Source location:")
  (println " " *source*)
  (println "Target location:")
  (println " " *target*)
  (println "Using Site URL:")
  (println " " *site-url*)

  ;; (doseq [s (source-files)]
  ;;   (pp/pprint s))


  (println "Assets:")
  (publish-assets!)
  (println "Articles:")
  (publish-articles!)
  (println "Index:")
  (publish-home!)
  )

(defn- cwd
  []
  (.getAbsolutePath (io/as-file (System/getProperty "user.dir"))))

(defn- parse
  [args]
  (cli/cli args
           ["-h" "--help" "Display this help message." :default false :flag true]
           ["-s" "--source" "Location of your site's site files." :default *source*]
           ["-u" "--url" "URL representing the site." :default *site-url*]
           ["-t" "--target" "Place to put your generated site." :default *target*]))

(defn -main
  "Application entry point."
  [& args]
  (let [[options trailing usage] (parse args)]

    (when (:help options)
      (println usage)
      (System/exit 0))

    (binding [*source* (.getAbsolutePath (io/as-file (:source options)))
              *target* (.getAbsolutePath (io/as-file (:target options)))]
      (do-run))))
