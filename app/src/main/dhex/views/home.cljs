(ns dhex.views.home
  (:require
   [re-frame.core :as rf :refer [dispatch]]
   [dhex.routes :as routes]
   [dhex.util :as u]
   [dhex.subs :as subs :refer [subscribe]]))

(defn tag-light
  [tag]
  (let [active-tag (subscribe :tag)]
    [:p.tag.tag-light {:class (str (when (= active-tag tag) "active"))
                       :on-click #(dispatch [:get-articles {:tag tag
                                                            :limit 10
                                                            :offset 0}])} tag]))

(defn tag-dark
  [tag]
  (let [active-tag (subscribe :tag)]
    [:p.tag.tag-dark  {:class (str (when (= active-tag tag) "active"))
                       :on-click #(dispatch [:get-articles {:tag tag
                                                            :limit 10
                                                            :offset 0}])} tag]))

(defn hero
  []
  [:section
   [:div.app-home-hero.h-60.flex.flex-col.items-center.justify-center.w-full.mb-6
    [:p.app-text-logo.text-7xl "DheX"]
    [:p "Big things start small"]]])

(defn feed-preview
  [{:keys [title description author tagList createdAt updatedAt]}]
  [:div.app-home-main-content-feeds-all-feeds-feed-preview

   ;;feed preview title section
   [:div.app-home-main-content-feeds-all-feeds-feed-preview-title.flex.items-center.gap-2
    [:img.app-home-main-content-feeds-all-feeds-feed-preview-title-author-avatar {:src (:image author)}]
    [:div
     [:p.app-home-main-content-feeds-all-feeds-feed-preview-title-username (:username author)]
     [:p.app-home-main-content-feeds-all-feeds-feed-preview-title-date
      (->  (if (seq updatedAt) createdAt updatedAt)
           u/get-date-string)]]]

   ;; feed preview Body Section
   [:div.app-home-main-content-feeds-all-feeds-feed-preview-body.flex.flex-col
    [:h2.app-home-main-content-feeds-all-feeds-feed-preview-body-title (u/split-at-dot title)]
    [:div.app-home-main-content-feeds-all-feeds-feed-preview-body-description description]]

;;feed preview Footer section
   [:div.app-home-main-content-feeds-all-feeds-feed-preview-footer.flex.justify-between.mb-4
    [:p.cursor-pointer "Read More...."]
    [:div.app-home-main-content-feeds-all-feeds-feed-preview-footer-tags.flex
     [:div.flex.gap-1
      (for [tag-item tagList]
        ^{:key tag-item} [tag-light tag-item])]]]])

(defn all-feeds
  [feeds]
  [:div.app-home-main-content-feeds-all-feeds
   (for [feed feeds]
     ^{:key (:slug feed)} [feed-preview feed])])

(defn tags-comp
  [{:keys [tags]}]

  [:div.flex.flex-wrap.gap-1.items-center
   (for [tag tags]
     ^{:key tag} [tag-dark tag])])

(defn pagination
  []
  (let [articles-count (subscribe :articles-count)
        tag (subscribe :tag)
        offset (subscribe :offset)
        pages (atom (range 1 (+ 1 articles-count) 10))]

    [:section.mx-auto {:class (str "w-11/12")}
     [:div.app-home-pagination.flex.flex-wrap.gap-1
      (for [page-inst (range 1 (inc (count @pages)))]
        (let [offset-param (dec (nth @pages (dec page-inst)))]

          ^{:key page-inst} [:div.app-home-pagination-page
                             {:class (when (= offset offset-param) "active")
                              :on-click #(if tag
                                           (dispatch [:get-articles {:tag tag
                                                                     :offset offset-param
                                                                     :limit 10}])
                                           (dispatch [:get-articles {:offset offset-param
                                                                     :limit 10}]))} [:p page-inst]]))]]))

(defn- main
  []
  (let [loading-articles? (subscribe :loading-articles?)
        articles (subscribe :articles)
        loading-tags?  (subscribe :loading-tags?)
        all-tags (subscribe :tags)
        filter (subscribe :filter)]

    [:section
     [:div.app-home-main.mx-auto {:class (str "w-11/12")}
      [:div..app-home-main-content.flex.gap-2.mb-8
       [:div.app-home-main-content-feeds.flex.flex-col
        [:div.app-home-main-content-feeds-selector.flex.gap-1
         [:button.p-3 {:class (when-not (or (:tag filter) (:feed filter)) "active")
                       :on-click #(dispatch [:get-articles {:limit 10 :offset 0}])} "Global Feed"]
         (when (:tag filter)  [:button.p-3 {:class "active"} (str "#" (:tag filter))])]
        [:div.flex
         (if loading-articles?
           [:div
            [:p "Loading Articles..........."]]
           [all-feeds articles])]]

       [:div.app-home-main-content-tags
        [:div.app-home-main-content-tags-content.p-3
         [:p.text-center.pb-2 "Popular Tags"]
         (if loading-tags?
           [:p "Loading Tags......"]
           [tags-comp all-tags])]]]
      (when-not loading-articles?
        [pagination])]]))

(defn home-view []
  (let [user nil ; (subscribe :name)
        ]
    [:div.app-home
     [hero]
     [main]]))

(defmethod routes/panels :home-view [] [home-view])
