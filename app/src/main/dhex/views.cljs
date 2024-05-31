(ns dhex.views
  (:require
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [dhex.routes :as routes]
   [dhex.subs :as subs]))

;; home

(def style {:cursor "pointer"})

(defn home-panel []
  (let [name nil; (subscribe [::subs/name])
        ]
    [:div
     [:h1
      (str "Hello from " name ". This is the Home Page.")]

     [:div
      [:div {:style style
             :on-click #(dispatch [:navigate :login])}
       "go to About Page"]]]))

(defmethod routes/panels :home-page [] [home-panel])

;; about

(defn login-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:div {:style style
           :on-click #(dispatch [:navigate :home])}
     "go to Home Page"]]])

(defmethod routes/panels :login-page [] [login-panel])

;; main

(defn main-panel []
  (let [active-page (subscribe [::subs/active-page])
        _ (println "Value for active page is " @active-page)
        prepared-active-page  (-> @active-page
                                  name
                                  (str "-page")
                                  keyword)]
    (println prepared-active-page  active-page 
             )
    (routes/panels prepared-active-page)
    )) 

