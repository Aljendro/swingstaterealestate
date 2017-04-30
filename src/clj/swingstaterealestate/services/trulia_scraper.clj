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
  (format trulia-website-url state (string/replace county #" " "_")))

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

(defn get-relative-info
  "Gets the link from the link to the property to trulia"
  [snippet]
  (->> snippet
    (map #(.select % "a"))
    (map #(.attr % "href"))
    (filter #(not (empty? %)))))

(defn zip-all-info
  "Zips all the info into individual maps"
  [address city prices info links]
  (map #(vector %1 %2 %3 %4 %5) address city prices info links))

(defn create-map-info
  "Zips the keywords for each column"
  [house-info]
  (map #(zipmap [:address :city :price :info :rel_link] %) house-info))

(defn expand-info
  "Takes the info key values and splits them into their own entries"
  [house-info]
  (let [info-str (string/split (get house-info :info "n/a n/a n/a") #" ")]
    (try
      (into
        (dissoc house-info :info)
        {:bedrooms (nth info-str 0)
         :baths (nth info-str 1)
         :square_ft (nth info-str 2)})
      (catch Exception e
        (into
          (dissoc house-info :info)
          {:bedrooms "n/a"
           :baths "n/a"
           :square_ft "n/a"})))))

(defn expand-all-info
  "Map over all info and expand the info values"
  [values]
  (map expand-info values))

(defn run-scraper
  "Runs the scraper for a given county and state"
  [county state]
  (let [webpage (get-trulia-webpage county state)
        house-list (get-houses-list webpage)
        addresses (get-address house-list)
        cities (get-city house-list)
        prices (get-price house-list)
        info (get-info house-list)
        rel-info (get-relative-info house-list)]
    (expand-all-info
      (create-map-info
        (zip-all-info addresses cities prices info rel-info)))))
