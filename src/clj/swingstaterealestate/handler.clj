(ns swingstaterealestate.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [swingstaterealestate.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [swingstaterealestate.env :refer [defaults]]
            [mount.core :as mount]
            [swingstaterealestate.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    #'service-routes
    (route/not-found
      "page not found")))


(defn app [] (middleware/wrap-base #'app-routes))
