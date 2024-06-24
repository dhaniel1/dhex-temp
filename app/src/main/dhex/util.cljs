(ns dhex.util
  (:require [clojure.string :as string :refer [join split]]
            [dhex.subs :as subs :refer [subscribe]]
            [re-frame.core :as rf :refer [dispatch]]))

(defn make-class
  [& args]
  (join " " args))

(defn get-date
  [date]
  (js/Date. date))

(defn get-year
  [date]
  (.getFullYear (get-date date)))

(defn get-date-string
  [date]
  (.toDateString (get-date date)))

(defn now
  []
  (js/Date.))

(defn current-year
  []
  (-> (now)
      (.getFullYear)))

(defn split-at-dot
  [string]
  (-> string
      (split #"\.")
      first
      (str ".")
      (string/capitalize)))

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

(defn alternative-view
  ([]
   (alternative-view nil))
  ([kw]
   [:div
    [:p "Nothing to show here!"]
    (cond
      (= kw :articles) [:p.cursor-pointer.link {:on-click #(dispatch [:navigate :editor :slug "new"])} "Create a new article?"]
      (= kw :tags) [:p.cursor-pointer  "Create a new tag?"]
      :else [:p.cursor.mt-5.text-base "Dont forget to smile."]) ;; this will most likely never be used
    ]))

(defn display-error
  [error]
  [:div.app [:p.error (str "An error occured: " error)]])
