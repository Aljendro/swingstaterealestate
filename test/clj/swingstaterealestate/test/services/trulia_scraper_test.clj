(ns swingstaterealestate.test.services.trulia-scraper-test
  (:require [clojure.test :refer :all]
            [swingstaterealestate.services.trulia-scraper :as trulia]
            [clojure.spec.test.alpha :as spec.test]
            [clojure.pprint :as pprint]))

(defn extract-results
  "Returns true when test passes, else the spec error"
  [fn-symbol]
  (let [result (first (spec.test/check fn-symbol))
        return-val (get-in result [:clojure.spec.test.check/ret :result])]
    return-val))

(defmacro spec-test [fn-symbol]
  `(let [sym# ~fn-symbol]
     (testing (str "Generatively testing " sym#)
       (is (= true (extract-results sym#))))))


(deftest test-trulia-scraper-generatively
  (spec-test `trulia/compose-url)
  (spec-test `trulia/create-map-info)
  (spec-test `trulia/expand-all-info)
  (spec-test `trulia/expand-info)
  (spec-test `trulia/get-address)
  (spec-test `trulia/get-city)
  (spec-test `trulia/get-elems)
  (spec-test `trulia/get-houses-list)
  (spec-test `trulia/get-info)
  (spec-test `trulia/get-price)
  (spec-test `trulia/get-relative-info)
  (spec-test `trulia/select-elements)
  (spec-test `trulia/zip-all-info))


