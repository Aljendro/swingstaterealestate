(ns swingstaterealestate.services.load-file
  (:require [cheshire.core :as cheshire]
            [clojure.java.io :as io]))


(defn load-2016-election-file []
  (with-open [in-file (io/reader "resources/data.json")]
    (cheshire/parse-stream in-file)))
