(ns dhex.views.article
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch]]
            [dhex.subs :as subs :refer [subscribe]]
            [dhex.util :as u]
            [dhex.routes :as routes]))

(defn article-meta
  [{:keys [slug  updatedAt createdAt author] :or {slug "" author {:username ""}}}]
  [:div.article-meta.flex.mt-10.gap-2
   [:img.article-meta-avatar {:src (:image author)
                              :alt "article-meta avatar"
                              :on-click (fn [event] (.preventDefault event)
                                          (dispatch [:navigate :profile :user-id (:username author)]))}]

   [:div.flex.flex-col
    [:p.cursor-pointer {:on-click (fn [event] (.preventDefault event)
                                    (dispatch [:navigate :profile :user-id (:username author)]))}
     (:username author)]
    [:p (u/get-date-string (if (seq updatedAt) createdAt updatedAt))]]

   [:div.flex.gap-2.ml-6
    [:button.app-button.small.transparent {:on-click #(dispatch [:navigate :editor {:slug slug}])} "Edit Article"]
    [:button.app-button.small.transparent.danger-border "Delete Article"]]])

(defn comment-form [comment]
  (fn []
    [:form.mt-14
     [:textarea.w-full
      {:id "article"
       :style {:border-bottom-right-radius "0px !important"
               :border-bottom-left-radius "0px !important"}
       :rows 4
       :placeholder "Enter your comment..."
       :on-change #(swap! comment assoc :body (-> % .-target .-value))
       :value (:body @comment)}]]))

(defn comment-component
  [{:keys [body  updatedAt createdAt author] :or {slug "" author {:username ""}}}]
  [:div.app_article-body-comment.mt-6
   [:div.app_article-body-comment-body.p-6 [:p body]]

   [:div.app_article-body-comment-bottom.flex.justify-between.pt-3
    [:div.flex.items-center.gap-2
     [:img.article-meta-avatar.small.cursor-default {:src (:image author)
                                                     :alt "article-meta avatar"}]
     [:p (:username author)]
     [:p.date (u/get-date-string (if (seq updatedAt) createdAt updatedAt))]]
    [:button.app-button.small.transparent.danger-border  "Delete Comment"]]])

(defn article-content [{:keys [title body tagList] :as active-article}]
  [:<>
   [:div.app_article-hero.h-60.flex.flex-col.items-start.justify-center.w-full.mb-6
    [:div.mx-auto.p-4 {:class (str "w-11/12")}
     [:p.text-5xl.font-semibold title]
     [article-meta active-article]]]

   [:section.mx-auto {:class (str "w-11/12")}
    [:div.app_article-body [:p.text-lg.font-normal.mb-10 body]
     [:div.app_article-taglist.flex.flex-wrap.gap-1.items-center
      (for [tag tagList]
        ^{:key tag} [u/tag-light tag])]]  ;; This tag should not be clickable

    [:div.flex.justify-center
     [article-meta active-article]]]])

(defn article-page []
  (let [default {:body ""}
        comment (r/atom default)
        post-comment (fn [event]
                       (.preventDefault event)
                       (dispatch [:post-comment {:body (:body @comment)}])
                       (reset! comment default))]

    (fn []
      (let [{:keys [title body tagList author] :as active-article} (subscribe [:active-article])
            user (subscribe [:user])
            article-comments (subscribe [:comments])]

        [:section.app
         [article-content active-article]

         [:div.app_article-body-comment
          [comment-form comment]
          [:div.app_article-body-comment-bottom.flex.justify-between
           [:img.article-meta-avatar {:src (:image author)
                                      :alt "article-meta avatar"}]
           [:button.app-button.small {:on-click #(post-comment %)} "Post Comment"]]]

         (when article-comments
           (for [article-comment article-comments]
             ^{:key (:id article-comment)} [comment-component article-comment]))]))))

(defmethod routes/panels :article-view [] [article-page])
