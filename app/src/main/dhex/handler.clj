(ns dhex.handler
  (:require  [clojure.string :as string :refer [join]]
             [compojure.core :refer [defroutes GET POST OPTIONS context wrap-routes]]
             [compojure.route :as route]
             [clj-http.client :as client]
             [clojure.pprint :as cp]
             [ring.middleware.params :refer [wrap-params]]
             [ring.middleware.cors :refer [wrap-cors]]
             [ring.middleware.reload :refer [wrap-reload]]
             [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
             [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def api-url "https://api.realworld.io/api")

(defn build-endpoint
  "concatinates api-url with / "
  [endpoint-base & args]
  (->>  args
        (cons endpoint-base)
        (join "")))

(defn wrap-api-url [handler]
  (fn [request]
    (let [uri (:uri request)
          params (:params request)
          url (build-endpoint api-url uri)]
      (handler (assoc request :url url :params params)))))

;; The clj-http call should be refactored eventually

;; Notes about this setup:
;; The server males hte calls and passes every appropriate header to and parameters to the client

(defn clj-request [request]
  (let [;; _ (clojure.pprint/pprint (get (:headers request) "authorization"))
        authorization (get-in request [:headers "authorization"])
        response (client/request {:method  (:request-method request)
                                  :url     (:url request)
                                  :headers {"authorization" authorization}
                                  :body    (:body request)
                                  :query-params (:query-params request)})]

    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (:body response)}))

(defn clj-post [request]
  (let [request (update request :headers dissoc "content-length")
        _ (clojure.pprint/pprint request)
        uri (:url request)
        response (client/post uri {:form-params (:body request)
                                   :content-type :json
                                   :headers {"Accept" "application/json"}})]

    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (:body response)}))

(defroutes app-routes
  (GET "/" [] "Hello, Clojure!")
  (GET "/about" [] "About Clojure")
  (-> (context "/" []
        (context "/articles" []
          (GET "/" [:as request] (clj-request request))
          (GET "/feed" [:as request] (clj-request request)))
        (GET "/tags" [:as request] (clj-request request))
        (context "/users" []
          (POST "/" [:as request] (clj-post request))
          (POST "/login" [:as request] (clj-post request))))

      (wrap-routes  wrap-api-url))
  ;; (OPTIONS "/*" [] (fn [_] {:status 200 :headers {} :body ""})) ; this should handle preflight requests
  (route/not-found "Not Found at all"))

(def handler
  (-> app-routes
      (wrap-cors :access-control-allow-origin #".*" ;;[#"http://localhost:8280"] 
                 :access-control-allow-credentials "true"
                 :access-control-allow-methods [:get :put :post :delete]
                 :access-control-allow-headers ["Content-Type" "Authorization" "Origin" "Accept"])
      (wrap-params)
      (wrap-defaults (dissoc site-defaults :security))
      (wrap-json-body {:keywords? true})
      (wrap-json-response)
      (wrap-reload)))
