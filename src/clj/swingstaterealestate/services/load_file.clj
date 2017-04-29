(ns swingstaterealestate.services.load-file
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


(defn load-2016-election-file []
  (with-open [in-file (io/reader "resources/data.csv")]
    (doall
      (csv/read-csv in-file))))
