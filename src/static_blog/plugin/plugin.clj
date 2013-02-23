(ns static-blog.plugin.plugin)

;; TODO: add a function to print out template vars the plugin is
;;       capable of fulfilling.

(defprotocol Plugin
  (concern [this])
  (publish! [this site]))
