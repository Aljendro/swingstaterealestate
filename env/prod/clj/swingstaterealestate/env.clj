(ns swingstaterealestate.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[swingstaterealestate started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[swingstaterealestate has shut down successfully]=-"))
   :middleware identity})
