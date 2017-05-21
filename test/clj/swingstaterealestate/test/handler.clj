(ns swingstaterealestate.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [swingstaterealestate.handler :refer :all]))

(deftest test-app
  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response)))))

  (testing "county results endpoint"
    (let [response ((app) (request :get "/api/results?state=CA"))]
      (is (= 200 (:status response)))))

  (testing "county results requires query parameter"
    (let [response ((app) (request :get "/api/results"))]
      (is (= 400 (:status response)))))

  (testing "housing results endpoint"
    (let [response ((app) (request :get "/api/housing?county=Orange&state=CA"))]
      (is (= 200 (:status response)))))

  (testing "housing results must have query params"
    (let [response ((app) (request :get "/api/housing"))]
      (is (= 400 (:status response))))))
