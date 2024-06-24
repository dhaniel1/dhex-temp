(ns dhex.views.settings
  (:require  [reagent.core :as r]
             [re-frame.core :as rf :refer [dispatch]]
             [dhex.routes :as routes]
             [dhex.util :as u]
             [dhex.subs :as subs :refer [subscribe]]))

(defn settings-panel
  []
  (let [{:keys [bio email image username]} (subscribe :user)
        default {:bio bio
                 :email email
                 :image image
                 :username username}
        cred (r/atom default)]

    (fn []
      (let [loading-update-user? (subscribe :loading-update-user?)
            onClick (fn [key] (swap! cred update key #(not %)))
            onChange (fn [event key]  (swap! cred assoc key (-> event .-target .-value)))
            onSubmit (fn [event credentials]
                       (.preventDefault event)
                       (dispatch [:update-user credentials]))]

        [:div.app_settings

   ;; Title Component
         [:section
          [:div.app_settings-title.flex.flex-col.justify-center.mb-6.mx-auto {:class (str "w-11/12")}
           [:h1 "Settings"]]]

;; Form components
         [:section.app
          [:div.app_settings-body.flex.flex-col.mx-auto {:class (str "w-11/12")}
           [:form.app-register-body-form.flex.flex-col.gap-6 {:on-submit #(onSubmit % (dissoc @cred :password-visible))}

            [:input.app_settings-body-form-input.w-full {:id "avatar-url"
                                                         :type "text"
                                                         :placeholder "Url of profile picture"
                                                         :on-change #(onChange % :image)
                                                         :default-value (:image @cred)
                                                         :value (:image @cred)}]

            [:input.app_settings-body-form-input.w-full {:id "name"
                                                         :type "text"
                                                         :placeholder "Your name"
                                                         :on-change #(onChange % :username)
                                                         :default-value (:username @cred)
                                                         :value (:username @cred)}]

            [:textarea.app_settings-body-form-input.w-full {:id "bio"
                                                            :type "text"
                                                            :rows 10
                                                            :placeholder "Short bio for you"
                                                            :default-value (:bio @cred)
                                                            :on-change #(onChange % :bio)
                                                            :value (:bio @cred)}]

            [:input.app_settings-body-form-input.w-full {:id "email"
                                                         :type "text"
                                                         :placeholder "Your email"
                                                         :on-change #(onChange % :email)
                                                         :default-value (:email @cred)
                                                         :value (:email @cred)}]

            [:fieldset.flex.flex.items-center
             [:input.app-register-body-form-input.w-full {:id "username"
                                                          :type (if (:password-visible @cred) "text" "password")
                                                          :placeholder "Enter your password"
                                                          :on-change  #(onChange % :password)
                                                          :default-value (:password @cred)
                                                          :value (:password @cred)}]

             [:div.is-visible {:on-click #(onClick :password-visible)
                               :class (if (:password-visible @cred) "yes-visible" "not-visible")}]]

            [:button.app-button.items-end {:disabled loading-update-user?}
             (if loading-update-user? "Updating User..." "Update User")]]]]]))))

(defmethod routes/panels :settings-view [] [settings-panel])
