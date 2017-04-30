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

(defn select-elements
  "Select the elements with a specified css selector"
  [snippet css]
  (->> snippet
    (map #(.select % css))
    (filter #(not (empty? %)))
    (map #(.text %))))

(defn get-address
  "Get the address from the object"
  [snippet]
  (select-elements snippet "span[itemprop$=streetAddress]"))

(defn get-city
  "Get the address from the object"
  [snippet]
  (select-elements snippet "span[itemprop$=addressLocality]"))

(defn get-price
  "Get the price from the object"
  [snippet]
  (select-elements snippet "span.cardPrice"))

(defn get-info
  "Get the info from the object"
  [snippet]
  (select-elements snippet "div.cardDetails ul"))

(defn zip-all-info
  "Zips all the info into individual maps"
  [address city prices info]
  (map #(vector %1 %2 %3 %4) address city prices info))

(defn create-map-info
  "Zips the keywords for each column"
  [house-info]
  (map #(zipmap [:address :city :price :info] %) house-info))

(defn run-scraper
  "Runs the scraper for a given county and state"
  [county state]
  (let [webpage (get-trulia-webpage county state)
        house-list (get-houses-list webpage)
        addresses (get-address house-list)
        cities (get-city house-list)
        prices (get-price house-list)
        info (get-info house-list)]
    (create-map-info (zip-all-info addresses cities prices info))))
