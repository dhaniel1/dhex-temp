(ns dhex.views.editor
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch]]
            [clojure.string :as string :refer [join trim split]]
            [dhex.subs :as subs :refer [subscribe]]
            [dhex.routes :as routes]))

(defn editor-page
  []
  (let [{:keys [title description body tagList slug] :as active-article} (subscribe [:active-article])
        tagList (join " " tagList)
        default {:title title :description description :body body :tagList tagList}
        content (r/atom default)]

    (fn []
      (let [loading-article? (subscribe :loading-article?)
            onChange (fn [event key] (swap! content assoc key (-> event .-target .-value)))
            onSubmit (fn [event content slug]
                       (.preventDefault event)
                       (dispatch [:upsert-article {:slug    slug
                                                   :article {:title       (trim (or (:title content) ""))
                                                             :description (trim (or (:description content) ""))
                                                             :body        (trim (or (:body content) ""))
                                                             :tagList     (split (:tagList content) #" ")}}]))]

        [:div.app_settings

   ;; Title Component
         [:section
          [:div.app_settings-title.flex.flex-col.justify-center.mb-6.mx-auto {:class (str "w-11/12")}
           [:h1 (str (if false "Update" "Create new") " article") ""]]]

;; Form components
         [:section.app
          [:div.app_settings-body.flex.flex-col.mx-auto {:class (str "w-11/12")}
           [:form.app-register-body-form.flex.flex-col.gap-6 {:on-submit #(onSubmit % @content slug)}

            [:input.app_settings-body-form-input.w-full {:id "title"
                                                         :type "text"
                                                         :placeholder "Article title"
                                                         :on-change #(onChange % :title)
                                                      ;; :default-value (:image @cred)
                                                         :value (:title @content)}]

            [:input.app_settings-body-form-input.w-full {:id "about"
                                                         :type "text"
                                                         :placeholder "About Article"
                                                         :on-change #(onChange % :description)
                                                      ;; :default-value (:article @cred)
                                                         :value (:description @content)}]

            [:textarea.app_settings-body-form-input.w-full {:id "article"
                                                            :type "text"
                                                            :rows 10
                                                            :placeholder "Write your article in markdown"
                                                         ;; :default-value (:article @cred)
                                                            :on-change #(onChange % :body)
                                                            :value (:body @content)}]

            [:input.app_settings-body-form-input.w-full {:id "tags"
                                                         :type "text"
                                                         :placeholder "Your tags"
                                                         :on-change #(onChange % :tagList)
                                                      ;; :default-value (:tags @cred)
                                                         :value (:tagList @content)}]

            [:button.app-button.items-end {:disabled loading-article?}
             (if active-article
               (if loading-article? "Updating...."  "Update Article")
               (if loading-article? "Publishing...."  "Publish Article"))]]]]]))))

(defmethod routes/panels :editor-view [] [editor-page])

