(ns blog.main
  (:gen-class)
  (:require
    [clojure.tools.logging :refer [info]]
    [clojure.edn :as edn :only [read-string]]
    [clojure.string :as string]
    [clojure.java.io :as io]
    [clojure.java.shell :as shell]
    [hiccup.page :refer [html5 include-css]]))

(defn- markdown!
  [string]
  (:out (shell/sh "/usr/local/bin/mmd" "--notes" "--smart" :in string)))

(defn- page
  [title text]
  (html5
    [:head
     [:title title]
     (include-css "style.css")]
    [:body
     [:header "..."]
     [:article
      [:h1 title]
      [:section]]
     [:footer "..."]]))

(defn- index-page
  [pages]
  (html5
    [:head
     [:title "The Web Blog"]
     (include-css "style.css")]
    [:body
     [:header "..."]
     [:section#indexes
      [:h1 "Posts"]]
     (for [p pages]
       [:section.page
        [:h1 (:title p)]
        ;; Side-fx should only be in main.
        (markdown! (:text p))])
     [:footer
      [:div.copyright "&copy; 2009-2014 Keith Irwin. All rights reserved."]]]))

(defn- load-text
  [f]
  (loop [headers []
         lines (string/split (slurp f) #"\n")]
    (if (empty? (string/trim (first lines)))
      (into (read-string (string/join " " headers))
            {:text (string/join "\n" (rest lines))})
      (recur (conj headers (first lines)) (next lines)))))

(defn- load-index
  []
  (->> (file-seq (io/as-file (io/resource "posts")))
       (filter (fn [f] (.isFile f)))
       (mapv load-text)))

(defn- scoop-pages
  [index]
  (let [pages (filter #(= (:type %) :page) index)]
    (map #(select-keys % [:title :text]) pages)))

(defn -main
  [& args]
  (info "Bootstrap")

  (let [index (load-index)]
    (info  (index-page (scoop-pages index))))

  (info "Sorry, nothing to see here anymore.")
  (System/exit 0))
