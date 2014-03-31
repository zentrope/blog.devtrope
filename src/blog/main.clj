(ns blog.main
  (:gen-class)
  (:require
    [clojure.tools.logging :refer [info]]
    [clojure.edn :as edn :only [read-string]]
    [clojure.string :as string]
    [clojure.java.io :as io]))

(defn -main
  [& args]
  (info "Bootstrap")
  (doseq [f (file-seq (io/as-file (io/resource "posts")))]
    (if (.isFile f)
      (info (:type (read-string (slurp f))))))
  (info "Sorry, nothing to see here anymore."))
