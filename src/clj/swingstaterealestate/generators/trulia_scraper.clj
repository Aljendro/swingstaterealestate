(ns swingstaterealestate.generators.trulia-scraper
  (:require [clojure.spec.gen.alpha :as spec.gen]
            [clojure.spec.alpha :as spec])
  (:import (org.jsoup.nodes Element)))


(defn element-generator
  "Generate Element objects"
  []
  (spec.gen/fmap #(Element. %) (spec/gen #{"a" "html" "body" "li" "ul"})))


(defn county-generator
  "Generate some counties"
  []
  (spec/gen #{"Orange" "Los Angeles"}))


(defn state-generator
  "Generate some states"
  []
  (spec/gen #{"CA"}))
