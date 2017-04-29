(ns swingstaterealestate.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))


(defschema Result
  {(s/optional-key :id) s/Int
   (s/optional-key :votes) double
   (s/optional-key :votes_dem) double
   (s/optional-key :votes_gop) double
   (s/optional-key :total_votes) double
   (s/optional-key :per_dem) double
   (s/optional-key :per_gop) double
   (s/optional-key :diff) double
   (s/optional-key :per_point_diff) s/Str
   (s/optional-key :state) s/Str
   (s/optional-key :county_name) s/Str
   (s/optional-key :combined_fips) s/Str})


(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}

  (context "/api" []
    :tags ["api" "real estate" "swing states"]

    (GET "/plus" []
      :return       Long
      :query-params [x :- Long, {y :- Long 1}]
      :summary      "x+y with query-parameters. y defaults to 1."
      (ok (+ x y)))

    (GET "/results" []
      :return)))
