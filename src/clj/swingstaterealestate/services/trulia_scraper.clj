(ns swingstaterealestate.services.trulia-scraper
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]
            [clojure.string :as string]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gs]
            [clojure.spec.test.alpha :as ts])
  (:import (java.net URL)
           (org.jsoup Jsoup)
           (org.jsoup.select Elements)
           (org.jsoup.nodes Element)))

;; Practicing using clojure.spec, the composeable specification library
;; Hopefully in the future we are able to statically analyze the specs
;; and use them as type hints to the compiler for more efficient code
;; generation.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; specifications ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn java-spec
  "Helper function to compose java instance specs"
  [class-name]
  (fn [object] (instance? class-name object)))

;; Cascade style sheet selectors are just strings
(s/def ::css string?)
;; Building up the structure of a house
(s/def ::address string?)
(s/def ::city string?)
(s/def ::price string?)
(s/def ::rel_link string?)
(s/def ::info string?)
(s/def ::bedrooms string?)
(s/def ::baths string?)
(s/def ::square_ft string?)
(s/def ::info string?)

(s/def ::house (s/keys :req [::address ::city ::price ::rel_link]))
(s/def ::house-info (s/merge ::house (s/keys :req [::info])))
(s/def ::house-expand-info (s/merge ::house (s/keys :req [::bedrooms ::baths ::square_ft])))

;; A macro that writes specs for me!
;; Can we use a function here?
(defmacro result-spec
  "Takes a the name of the function and returns a spec
  which takes an Element object  and returns a list of results
  as strings"
  [fn-name]
  `(s/fdef ~fn-name
           :args (s/* (java-spec Element))
           :ret (s/* string?)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; implementations ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def trulia-website-url
  "http://www.trulia.com/County/%s/%s_Real_Estate/")


;; We can further spec what it means to be a county
;; and what is means to be a state, but here we just
;; make it simple and accept a string...
(s/fdef compose-url
        :args (s/cat :county string? :state string?)
        :ret string?)
(defn compose-url
  "Composes a url using a county and state"
  [county state]
  (format trulia-website-url state (string/replace county #" " "_")))


(s/fdef get-elems
        :args (s/cat :page (java-spec Element) :css ::css)
        :ret (java-spec Elements))
(defn get-elems
  "Gets all elements that are selected by the css string"
  [page css]
  (.select page css))


(s/fdef get-trulia-webpage
        :args (s/cat :county string? :state string?)
        :ret (java-spec Element))
(defn get-trulia-webpage
  "Use a county name and state to get an html page"
  [county-name state]
  (let [page (.get (Jsoup/connect (compose-url county-name state)))]
    page))


(s/fdef get-houses-list
        :args (s/cat :page (java-spec Element))
        :ret (s/* (java-spec Element)))
(defn get-houses-list
  "Using the dom object, return the html
  that contains the house information"
  [page]
  (let [elements (get-elems page "ul.mvn.containerFluid > li")]
    (for [e elements] e)))


(s/fdef select-elements
        :args (s/cat :element (java-spec Element) :css string?)
        :ret (s/* string?))
(defn select-elements
  "Select the elements with a specified css selector"
  [snippet css]
  (->> snippet
    (map #(get-elems % css))
    (filter #(not (empty? %)))
    (map #(.text %))))


;; Keeping it DRY by having a macro expand to the same type
;; of spec.
(result-spec get-address)
(defn get-address
  "Get the address from the Element"
  [snippet]
  (select-elements snippet "span[itemprop$=streetAddress]"))


(result-spec get-city)
(defn get-city
  "Get the city from the Element"
  [snippet]
  (select-elements snippet "span[itemprop$=addressLocality]"))


(result-spec get-price)
(defn get-price
  "Get the price from the Element"
  [snippet]
  (select-elements snippet "span.cardPrice"))


(result-spec get-info)
(defn get-info
  "Get the info from the Element"
  [snippet]
  (select-elements snippet "div.cardDetails ul"))


(result-spec get-relative-info)
(defn get-relative-info
  "Gets the link from the <a> tag from each property"
  [snippet]
  (->> snippet
    (map #(get-elems % "a"))
    (map #(.attr % "href"))
    (filter #(not (empty? %)))))


(s/fdef zip-all-info
        :args (s/* (s/* string?))
        :ret (s/* (s/* string?)))
(defn zip-all-info
  "Zips all the info into individual maps"
  [address city prices info links]
  (map #(vector %1 %2 %3 %4 %5) address city prices info links))


(s/fdef create-map-info
        :args (s/* sequential?)
        :ret (s/* ::house-info))
(defn create-map-info
  "Zips the keywords for each column"
  [house-info]
  (map #(zipmap [:address :city :price :info :rel_link] %) house-info))


(s/fdef expand-info
        :args (s/cat :house ::house-info)
        :ret ::house-expand-info)
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


(s/fdef expand-all-info
        :args (s/cat :houses (s/* ::house-info))
        :ret (s/* ::house-expand-info))
(defn expand-all-info
  "Map over all info and expand the info values"
  [values]
  (map expand-info values))


(s/fdef run-scraper
        :args (s/cat :county string? :state string?)
        :ret (s/* ::house-expand-info))
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
