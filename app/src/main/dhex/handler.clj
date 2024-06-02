(ns dhex.handler
  (:require  [clojure.string :as string :refer [join]]
             [compojure.core :refer [defroutes GET]]
             [compojure.route :as route]
             [clj-http.client :as client]
             [ring.middleware.cors :refer [wrap-cors]]
             [ring.middleware.reload :refer [wrap-reload]]
             [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            ;[ring.util.response :as res]
             ))

(def api-url "https://api.realworld.io/api")

(defn build-endpoint
  "concatinates api-url with / "
  [endpoint-base & args]
  (->>  (map #(string/replace % "/" "") args)
        (cons endpoint-base)
        (join "/")))

(defn wrap-api-url [handler]
  (fn [request]
    (let [uri (-> request :uri)
          endpoint (build-endpoint api-url uri)]
      (println "new enpoiint build: " endpoint)
      (handler (assoc request :main-uri endpoint)))))

;; The clj-http call should be refactored eventually

(defn fetch-articles [request]
  (let [response (client/get request)
       ; _ (println "request: " request)
        ]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (:body response)}))

(defroutes app-routes
  (GET "/" [] "Hello, Clojure!")
  (GET "/about" [] "About Clojure")
  (wrap-api-url
   (GET "/articles" {:keys [main-uri]} (fetch-articles main-uri)))
  (route/not-found "Not Found"))

(def handler
  (-> app-routes
      (wrap-defaults (dissoc site-defaults :security))
      (wrap-cors :access-control-allow-origin [#"http://localhost:8280"]
                 :access-control-allow-credentials "true"
                 :access-control-allow-methods [:get :put :post :delete])
      (wrap-reload)))
