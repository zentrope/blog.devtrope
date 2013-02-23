(ns static-blog.plugin.assets
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [static-blog.plugin.plugin :as plugin]
   [static-blog.lib.utils :as utils]))

(def ^{:private true :dynamic true} *site* {})

(defn- assets
  [from to]
  (->> (io/as-file from)
       (file-seq)
       (map #(.getAbsolutePath %))
       (map #(into {} {:target (.replace % from to)
                       :source %}))))

(defn- htmlish?
  [f]
  (let [name (string/lower-case (.getName f))]
    (or (.endsWith name "html")
        (.endsWith name "css")
        (.endsWith name "js"))))

(defn- publish-assets!
  []

  (let [{:keys [source-dir asset-dir target-dir]} *site*
       from (utils/path-from-vec source-dir asset-dir)]

    (doseq [{:keys [source target]} (assets from target-dir)]
      (let [from (io/as-file source)
            to (io/as-file target)]
        (println " - publishing" to)
        (when (.isDirectory from)
          (.mkdirs to))
        (when (.isFile from)
          (.mkdirs (.getParentFile to))
          (if (htmlish? from)
            (spit to (utils/merge-template (slurp from) *site*))
            (io/copy from to)))))))

(deftype AssetPlugin []
  plugin/Plugin
  (concern [this]
    "Asset Plugin")
  (publish! [this site]
    (binding [*site* site]
      (publish-assets!))))

(defn mk-plugin
  []
  (AssetPlugin.))
