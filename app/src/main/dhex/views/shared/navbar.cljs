(ns dhex.views.shared.navbar
  (:require [re-frame.core :as rf :refer [dispatch]]
            [dhex.subs :as subs :refer [subscribe]]
            [dhex.routes :as routes :refer [url-for]]))

(defn navbar
  []
  (let [active-page (subscribe :active-page)
        user (subscribe :user)]
    [:section.navbar-section
     [:nav.app-navbar.flex.justify-between.items-center.p-5.mx-auto {:class (str "w-11/12")}
      [:p.app-text-logo.text-5xl.cursor-pointer {:on-click #(dispatch [:navigate :home])} "DheX"]
      [:div.app-navbar-navlinks.flex.gap-3.text-lg
       [:p.link {:class (when (= active-page :home) "active")
                 :on-click #(dispatch [:navigate :home])} "Home"]
       (if (empty? user)

         [:<>
          [:p.link {:class (when (= active-page :login) "active")
                    :on-click #(dispatch [:navigate :login])} "Sign In"]
          [:p.link {:class (when (= active-page :register) "active")
                    :on-click #(dispatch [:navigate :register])} "Sign Up"]]

         [:<>
          [:p.link {:class (when (= active-page :editor) "active")
                    :on-click #(dispatch [:navigate :editor :slug "new"])} "New Article"]
          [:p.link {:class (when (= active-page :settings) "active")
                    :on-click #(dispatch [:navigate :settings])} "Settings"]
          [:a.link {:class (when (= active-page :profile) "active")
                    :on-click #(dispatch [:navigate :profile :user-id (-> user :username)])} "Profile"]
          [:button.app-button.small.danger.items-end {:on-click #(dispatch [:logout])} "Logout"]])]]]))
