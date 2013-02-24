(ns static-blog.task.serve
  ;;
  ;; Serves up the generated web-site so you can test it as if it were
  ;; running on a "real" site.
  ;;
  (:require
   [static-blog.task.task :as task])
  ;;
  (:require
   [ring.middleware.file-info :as file-info]
   [ring.middleware.file :as file]
   [ring.adapter.jetty :as jetty]))

(defn- app
  [docroot]
  (-> (fn [r] {})
      (file/wrap-file docroot)
      (file-info/wrap-file-info)))

(defn- start-httpd
  [port docroot url]
  (println " - docroot:" docroot)
  (println " - port:   " port)
  (println " - url:    " (str  "http://localhost:" port url "/"))
  (jetty/run-jetty (app docroot) {:port port :join? true}))

(deftype ServeTask []
  task/Task
  (concern [this]
    "Serve Task")
  (invoke! [this site]
    (let [port (get-in site [:server :port])
          docroot (:docroot site)
          url (:site-url site)]
      (start-httpd port docroot url))))

(defn mk-task
  []
  (ServeTask.))
