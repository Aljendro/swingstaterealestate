(ns swingstaterealestate.services.trulia-scraper
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]
            [clojure.string :as string])
  (:import (java.net URL)
           (org.jsoup Jsoup)
           (org.jsoup.select Elements)
           (org.jsoup.nodes Element)))

(def trulia-website-url
  "http://www.trulia.com/County/%s/%s_Real_Estate/")

(defn compose-url
  "Composes a url using a county and state"
  [county state]
  (format trulia-website-url state (first (string/split county #" "))))

(defn get-elems [page css]
  (.select page css))

(defn get-trulia-webpage
  "Use a county name and state to get an html page"
  [county-name state]
  (let [page (.get (Jsoup/connect (compose-url county-name state)))]
    page))

(defn get-houses-list
  "Using the dom object, return the html
  that contains the house information"
  [page]
  (let [elements (get-elems page "ul.mvn.containerFluid > li")]
    (for [e elements] e)))

(defn get-address
  "Get the address from the object"
  [snippet]
  (->> snippet
    (map #(.select % "span[itemprop$=streetAddress]"))
    (filter #(not (empty? %)))
    (map #(.text %))))

(defn get-city
  "Get the address from the object"
  [snippet]
  (->> snippet
    (map #(.select % "span[itemprop$=addressLocality]"))
    (filter #(not (empty? %)))
    (map #(.text %))))

(defn zip-all-info
  "Zips all the info into individual maps"
  [address city]
  (map #(vector %1 %2) address city))

(defn create-map-info
  "Zips the keywords for each column"
  [house-info]
  (map #(zipmap [:address :city] %) house-info))

(defn run-scraper
  "Runs the scraper for a given county and state"
  [county state]
  (let [webpage (get-trulia-webpage county state)
        house-list (get-houses-list webpage)
        addresses (get-address house-list)
        cities (get-city house-list)]
    (create-map-info (zip-all-info addresses cities))))
