(ns dhex.views.register
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch]]
            [dhex.subs :as subs :refer [subscribe]]
            [dhex.routes :as routes]))

#_(def cred (r/atom {:name "" :email "" :password "" :password-visible false}))

#_(defn onClick
    [key]
    (swap! cred update key #(not %)))

#_(defn onChange [event key]
    (swap! cred assoc key (-> event .-target .-value)))

(defn register-page
  []
  (let [cred (r/atom {:username "" :email "" :password "" :password-visible false})]

    (fn []
      (let [;; registering (subscribe :registering)
            onClick (fn [key] (swap! cred update key #(not %)))
            onChange (fn [event key]  (swap! cred assoc key (-> event .-target .-value)))
            register (fn [event credentials]
                       (.preventDefault event)
                       (dispatch [:register-user credentials]))]

        [:div.app-register

   ;; Title Component
         [:section
          [:div.app-register-title.flex.flex-col.justify-center.mx-auto {:class (str "w-11/12")}
           [:h1 "Sign up"]
           [:p.text-justify {:on-click #(dispatch [:navigate :register])} "Have an account?"]]]

   ;; Form components
         [:section
          [:div.app-register-body.flex.flex-col.mx-auto {:class (str "w-11/12")}
           [:form.app-register-body-form.flex.flex-col.gap-6 {:on-submit #(register % (dissoc @cred :password-visible))}

            [:input.app-register-body-form-input.w-full {:id "name"
                                                         :type "text"
                                                         :placeholder "Enter your name"
                                                         :on-change #(onChange % :username)
                                                         :value (:username @cred)}]

            [:input.app-register-body-form-input.w-full {:id "email"
                                                         :type "text"
                                                         :placeholder "Enter your email"
                                                         :on-change #(onChange % :email)
                                                         :value (:email @cred)}]

            [:fieldset.flex.flex.items-center
             [:input.app-register-body-form-input.w-full {:id "username"
                                                          :type (if (:password-visible @cred) "text" "password")
                                                          :placeholder "Enter your password"
                                                          :on-change  #(onChange % :password)
                                                          :value (:password @cred)}]

             [:div.is-visible {:on-click #(onClick :password-visible)
                               :class (if (:password-visible @cred) "yes-visible" "not-visible")}]]
            [:button.app-button.items-end ;; implement it's disabled state
             "Update Settings"]]]]

;; Form Input components
         ]))))
(defmethod routes/panels :register-view [] [register-page])
