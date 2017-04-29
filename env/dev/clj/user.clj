(ns user
  (:require [mount.core :as mount]
            swingstaterealestate.core))

(defn start []
  (mount/start-without #'swingstaterealestate.core/repl-server))

(defn stop []
  (mount/stop-except #'swingstaterealestate.core/repl-server))

(defn restart []
  (stop)
  (start))


