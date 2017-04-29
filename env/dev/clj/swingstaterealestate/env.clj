(ns swingstaterealestate.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [swingstaterealestate.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[swingstaterealestate started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[swingstaterealestate has shut down successfully]=-"))
   :middleware wrap-dev})
