(ns dhex.views.shared.navbar
  (:require [re-frame.core :as rf :refer [dispatch]]
            [dhex.subs :as subs :refer [subscribe]]))

(defn navbar
  []
  (let [active-page (subscribe :active-page)]
    [:section.navbar-section
     [:nav.app-navbar.flex.justify-between.items-center.p-5.mx-auto {:class (str "w-11/12")}
      [:p.app-text-logo.text-3xl.cursor-pointer {:on-click #(dispatch [:navigate :home])} "DheX"]
      [:div.app-navbar-navlinks.flex.gap-3.text-lg
       [:p.link {:class (when (= active-page :home) "active")
                 :on-click #(dispatch [:navigate :home])} "Home"]
       [:p.link {:class (when (= active-page :login) "active")
                 :on-click #(dispatch [:navigate :login])} "Sign In"]
       [:p.link {:class (when (= active-page :register) "active")
                 :on-click #(dispatch [:navigate :register])} "Sign Up"]]]]))
