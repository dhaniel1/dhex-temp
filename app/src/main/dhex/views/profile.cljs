(ns dhex.views.profile
  (:require
   [re-frame.core :as rf :refer [dispatch]]
   [dhex.routes :as routes]
   [dhex.util :as u  :refer [alternative-view tag-light tag-dark]]
   [dhex.subs :as subs :refer [subscribe]]))

(defn hero
  []
  (let [{:keys [image username bio]} (subscribe :profile)
        user (subscribe :user)]
    [:section
     [:div.app_profile-hero.h-fit.flex.flex-col.items-center.justify-center.w-full.mb-6.pt-10
      [:img {:src image :width "3rem" :height "3rem"}]
      [:p username]
      [:p.text.sm bio]
      (if false ;; (= (:username user) username)
        [:button.app-button.transparent.self-end.mb-5 {:class "mr-[10rem]"} "Edit profile settings"]
        [:button.app-button.transparent.self-end.mb-5 {:class "mr-[10rem]"} "Implement follow user feature "])]]))

(defn feed-preview
  [{:keys [title description slug author tagList createdAt updatedAt]
    :or {slug ""
         author {:username ""}}}]

  [:div.app_profile-main-content-feeds-all-feeds-feed-preview

   ;;feed preview title section
   [:div.app_profile-main-content-feeds-all-feeds-feed-preview-title.flex.items-center.gap-2
    [:img.app_profile-main-content-feeds-all-feeds-feed-preview-title-author-avatar.cursor-pointer
     {:src (:image author)
      :alt "profile avatar"
      :on-click (fn [event] (.preventDefault event)
                  (dispatch [:navigate :profile :user-id (:username author)]))}]

    [:div
     [:p.app_profile-main-content-feeds-all-feeds-feed-preview-title-username.cursor-pointer
      {:on-click (fn [event] (.preventDefault event)
                   (dispatch [:navigate :profile :user-id (:username author)]))}
      (:username author)]

     [:p.app_profile-main-content-feeds-all-feeds-feed-preview-title-date
      (->  (if (seq updatedAt) createdAt updatedAt)
           u/get-date-string)]]]

   ;; feed preview Body Section
   [:div.app_profile-main-content-feeds-all-feeds-feed-preview-body.flex.flex-col.cursor-pointer
    {:on-click (fn [event] (.preventDefault event)
                 (dispatch [:navigate :article :slug slug]))}
    [:h2.app_profile-main-content-feeds-all-feeds-feed-preview-body-title (u/split-at-dot title)]
    [:div.app_profile-main-content-feeds-all-feeds-feed-preview-body-description description]]

;;feed preview Footer section
   [:div.app_profile-main-content-feeds-all-feeds-feed-preview-footer.flex.justify-between.mb-4
    [:p.cursor-pointer {:on-click (fn [event] (.preventDefault event)
                                    (dispatch [:navigate :article :slug slug]))}
     "Read More...."]
    [:div.app_profile-main-content-feeds-all-feeds-feed-preview-footer-tags.flex
     [:div.flex.gap-1
      (for [tag-item tagList]
        ^{:key tag-item} [tag-light tag-item])]]]])

(defn all-feeds
  [feeds]
  [:div.app_profile-main-content-feeds-all-feeds
   (if (seq feeds)
     (for [feed feeds]
       ^{:key (:slug feed)} [feed-preview feed])
     [alternative-view :articles])])

(defn tags-comp
  [{:keys [tags]}]

  [:div.flex.flex-wrap.gap-1.items-center
   (for [tag tags]
     ^{:key tag} [tag-dark tag])])

(defn pagination
  []
  (let [articles-count (subscribe :articles-count)
        tag (subscribe :tag)
        author (subscribe :author)
        offset (subscribe :offset)
        pages (atom (range 1 (+ 1 articles-count) 10))]

    [:section.mx-auto {:class (str "w-9/12")}
     [:div.app_profile-pagination.flex.flex-wrap.gap-1
      (for [page-inst (range 1 (inc (count @pages)))]
        (let [offset-param (dec (nth @pages (dec page-inst)))]

          ^{:key page-inst} [:div.app_profile-pagination-page
                             {:class (when (= offset offset-param) "active")
                              :on-click #(if tag
                                           (dispatch [:get-articles {:tag tag
                                                                     :offset offset-param
                                                                     :limit 10}])
                                           (dispatch [:get-articles {:author author
                                                                     :offset (or offset-param 0)
                                                                     :limit 10}]))}
                             [:p page-inst]]))]]))

(defn- main
  []
  (let [user (subscribe :user)
        loading-articles? (subscribe :loading-articles?)
        articles (subscribe :articles)
        author (subscribe :author)
        favorites (subscribe :favorites)
        articles-count (subscribe :articles-count)]

    (println "Userrrr::: " user)

    [:section
     [:div.app_profile-main.mx-auto {:class (str "w-11/12")}
      [:div.app_profile-main-content-feeds.flex.flex-col
       [:div.app_profile-main-content-feeds-selector.flex.gap-1

        [:button.p-3 {:class (when author "active")
                      :on-click #(dispatch [:navigate :profile :user-id (-> user :username)])}
         "My Articles"]

        [:button.p-3 {:class (when favorites "active")
                      :on-click #(dispatch [:navigate :favorited :user-id (-> user :username)])}
         "My Favourites"]]

       [:div
        (if loading-articles?
          [:div
           [:p "Loading Articles..........."]]
          [all-feeds articles])]]

      (when-not (or loading-articles? (< articles-count 10))
        [pagination])]]))

(defn profile-view []
  [:div.app_profile
   [hero]
   [main]])

(defmethod routes/panels :profile-view [] [profile-view])
