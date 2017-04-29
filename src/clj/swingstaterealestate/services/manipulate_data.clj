(ns swingstaterealestate.services.manipulate-data
  (:require [swingstaterealestate.services.load-file :as load]))


;; Load the csv data
(def election-data (load/load-2016-election-file))

(defn label-data
  "Labels is a vector of keys, and data
  is a vector of vectors. Each vector should be
  the same length as the vector of keys"
  [labels data]
  (into []
    (map (partial zipmap labels)) data))
