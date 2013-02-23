(ns static-blog.task.task)

;; TODO: add a function to print out template vars the plugin is
;;       capable of fulfilling.

(defprotocol Task
  (concern [this])
  (invoke! [this site]))
