(ns dhex.views.login
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch]]
            [dhex.routes :as routes]
            [dhex.subs :as sub :refer [subscribe]]))

(defn login-page []
  (let [cred (r/atom {:email "" :password "" :password-visible false})]

    (fn []

      (let [loading-login-user? (subscribe :loading-login-user?)
            register (fn [event cred]
                       (.preventDefault event)
                       (dispatch [:login-user cred]))
            onChange (fn [event key] (swap! cred assoc key (-> event .-target .-value)))
            onClick (fn [key] (swap! cred update key #(not %)))]

        [:div.app-login

   ;; Title Component
         [:section
          [:div.app-login-title.flex.flex-col.justify-center.mx-auto {:class (str "w-11/12")}
           [:h1 "Sign In"]
           [:p.text-justify {:on-click #(dispatch [:navigate :register])} "Need an account?"]]]

   ;; Form components
         [:section
          [:div.app-login-body.flex.flex-col.mx-auto {:class (str "w-11/12")}
           [:form.app-login-body-form.flex.flex-col.gap-6 {:on-submit #(register % (dissoc @cred :password-visible))}
            [:input.app-login-body-form-input.w-full {:id "email"
                                                      :type "text"
                                                      :placeholder "Enter your email"
                                                      :on-change #(onChange % :email)
                                                      :value (:email @cred)}]

            [:div.flex.items-center
             [:input.app-login-body-form-input.w-full {:id "username"
                                                       :type (if (:password-visible @cred) "text" "password")
                                                       :placeholder "Enter your password"
                                                       :on-change  #(onChange % :password)
                                                       :value (:password @cred)}]
             [:div.is-visible {:on-click #(onClick :password-visible)
                               :class (if (:password-visible @cred) "yes-visible"  "not-visible")}]]

            [:button.app-button.items-end {:disabled loading-login-user?}
             (if loading-login-user? "Signing in..." "Sign in")]]]]]))))

;; Form Input components
(defmethod routes/panels :login-view [] [login-page])
