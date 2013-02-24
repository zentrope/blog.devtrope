(ns static-blog.lib.site
  ;;
  ;; Convenience functions for extraction information out
  ;; of a site-description.
  ;;
  (:require
   [static-blog.lib.utils :as utils]))

(defn article-dir
  [site]
  (utils/path-from-keys site :source-dir :article-dir))

(defn template-dir
  [site]
  (utils/path-from-keys site :source-dir :template-dir))

(defn output-page
  [site page]
  (utils/path-from-vec (:target-dir site) (get-in site [page :output-page])))

(defn slurp-template
  [site page template]
  (->> (get-in site [page template])
       (utils/path-from-vec (template-dir site))
       (slurp)))