(ns dhex.views.settings
  (:require
   [re-frame.core :as rf :refer [dispatch]]
   [dhex.routes :as routes]
   [dhex.util :as u]
   [dhex.subs :as subs :refer [subscribe]]))

(defn settings-view []
  (let [user (subscribe :user)]
    [:div.app-home
     [:p.mt-5 "This is the settings page"]]))

(defmethod routes/panels :settings-view [] settings-view)
