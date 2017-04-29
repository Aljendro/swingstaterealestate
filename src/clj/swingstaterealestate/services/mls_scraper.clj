(ns swingstaterealestate.services.mls-scraper
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]
            [clojure.string :as string])
  (:import [java.net URL]))

(def mls-website-url
  "https://mls.foreclosure.com/search.html?q=%s,%s")

(defn compose-url
  "Composes a url using a county and state"
  [county state]
  (format mls-website-url (string/replace county #" " "%20") state))

(defn get-mls-webpage
  "Use a county name and state to get an html page"
  [county-name state]
  (let [dom (html/html-resource (URL. (compose-url county-name state)))]
    dom))

(defn get-houses-list
  "Using the dom object, return the html
  that contains the house information"
  [dom]
  (html/select dom [:#listingRowsHtml :tr]))

(defn get-address
  "Get the address from the object"
  [snippet]
  (let [address-snippet (html/select snippet [:td :p :a.address])]
    (map html/text address-snippet)))

(defn get-bedrooms
  "Gets the amount of bedrooms from the object"
  [snippet]
  (let [bedroom-snippet (html/select snippet [:td :p.dgbed])]
    (map html/text bedroom-snippet)))

(defn get-bathrooms
  "Gets the amount of bathrooms from the object"
  [snippet]
  (let [bathroom-snippet (html/select snippet [:td :p.dgbath])]
    (map html/text bathroom-snippet)))

(defn get-square-feet
  "Gets the amount of square footage from the object"
  [snippet]
  (let [square-ft-snippet (html/select snippet [:td :p.dgsize])]
    (map html/text square-ft-snippet)))

(defn zip-all-info
  "Zips all the info into individual maps"
  [addresses bedrooms bathrooms square-feet]
  (map #(vector %1 %2 %3 %4) addresses bedrooms bathrooms square-feet))

(defn create-map-info
  "Zips the keywords for each column"
  [house-info]
  (map #(zipmap [:address :num_bed :num_bath :square_ft] %) house-info))

(defn run-scraper
  "Runs the scraper for a given county and state"
  [county state]
  (let [webpage (get-mls-webpage county state)
        house-list (get-houses-list webpage)
        addresses (get-address house-list)
        bedrooms (get-bedrooms house-list)
        bathrooms (get-bathrooms house-list)
        feet (get-square-feet house-list)]
    (create-map-info (zip-all-info addresses bedrooms bathrooms feet))))
