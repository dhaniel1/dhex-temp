(ns dhex.handler
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] "Hello, Clojure!")
  (GET "/about" [] "About Clojure")
  (route/not-found "Not Found"))

(def handler
  (-> app-routes
      (wrap-defaults (dissoc site-defaults :security))
      (wrap-cors :access-control-allow-origin [#"http://localhost:8280"]
                 :access-control-allow-credentials "true"
                 :access-control-allow-methods [:get :put :post :delete])
      (wrap-reload)))
