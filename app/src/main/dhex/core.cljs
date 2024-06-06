(ns dhex.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as rf :refer [dispatch-sync]]
   [dhex.routes :as routes]
   [dhex.views.index :as index]
   [dhex.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [index/main-panel] root-el)))

(defn init []
  (routes/start!)
  (dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))
