(ns swingstaterealestate.routes.services
  (:require [swingstaterealestate.services.manipulate-data :as data]
            [swingstaterealestate.services.trulia-scraper :as trulia]
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))


(s/defschema Result
  {(s/required-key "per_dem") s/Str
   (s/required-key "per_gop") s/Str
   (s/required-key "county_name") s/Str})

(s/defschema HousingResult
  {(s/required-key :address) s/Str
   (s/required-key :city) s/Str
   (s/required-key :price) s/Str
   (s/required-key :baths) s/Str
   (s/required-key :bedrooms) s/Str
   (s/required-key :square_ft) s/Str})

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Swing State Real Estate API"
                           :description "Public Services"}}}}

  (context "/api" []
    :tags ["swing states"]

    (GET "/results" []
      :summary "Returns county voting results for US 2016 election"
      :return [Result]
      :query-params [state :- s/Str]
      (-> (ok (data/query-data state))
        (assoc-in [:headers "Access-Control-Allow-Origin"]  "*")
        (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELETE,OPTIONS")
        (assoc-in [:headers "Access-Control-Allow-Headers"] "X-Requested-With,Content-Type,Cache-Control")))

    (GET "/housing" []
      :summary "Returns housing data for a given county and state"
      :return [HousingResult]
      :query-params [county :- s/Str state :- s/Str]
      (-> (ok (trulia/run-scraper county state))
        (assoc-in [:headers "Access-Control-Allow-Origin"]  "*")
        (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELETE,OPTIONS")
        (assoc-in [:headers "Access-Control-Allow-Headers"] "X-Requested-With,Content-Type,Cache-Control")))))
