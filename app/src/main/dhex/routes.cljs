(ns dhex.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as rf :refer [reg-fx]]))

(defmulti panels identity)
(defmethod panels :default [] [:div "No panel found for this route."])

(def routes-cljs  ["/" {""         :home
                        "login"    :login
                        "logout"   :logout
                        "register" :register
                        "settings" :settings
                        "editor/"  {[:slug] :editor}
                        "article/" {[:slug] :article}
                        "profile/" {[:user-id] {""           :profile
                                                "/favorites" :favorited}}}])

(defn get-route-handler
  "Returns a map with a :handler key"
  [url]
  (bidi/match-route routes-cljs url))

(defn url-for
  [& args]
  (apply bidi/path-for (into [routes-cljs] args)))

(def history (let [dispatch #(rf/dispatch [:set-active-page {:page (:handler %)
                                                             :slug      (get-in % [:route-params :slug])
                                                             :profile   (get-in % [:route-params :user-id])
                                                             :favorited (get-in % [:route-params :user-id])}])

                   matcher #(get-route-handler %)]

               (pushy/pushy dispatch matcher)))

(defn start!
  []
  (pushy/start! history))

(defn navigate!
  "This receives the value of a handler as returned by match-route fn"
  [handler]
  (pushy/set-token! history (url-for handler)))

(reg-fx
 :navigate
 (fn [handler]
   (navigate! handler)))
