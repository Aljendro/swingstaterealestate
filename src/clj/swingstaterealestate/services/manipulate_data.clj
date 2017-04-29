(ns swingstaterealestate.services.manipulate-data
  (:require [swingstaterealestate.services.load-file :as load]))


;; Load the json data
(defonce loaded-data (load/load-2016-election-file))

(defn query-data
  "Labels is a vector of keys, and data
  is a vector of vectors. Each vector should be
  the same length as the vector of keys"
  [query-string]
  (get loaded-data query-string []))
