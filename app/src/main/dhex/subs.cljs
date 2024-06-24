(ns dhex.subs
  (:require
   [re-frame.core :as rf :refer [reg-sub]]))

(defn reverse-comp
  [a b]
  (compare b a))

(defn ensure-vec
  [arg]
  (when-not (nil? arg)
    (if (sequential? arg)
      (into [] arg)
      [arg])))

(defn ensure-ns
  [ns-kw sub-vect]
  (if-let [ns (namespace sub-vect)]
    sub-vect
    (keyword ns-kw sub-vect)))

(defn ns-first
  [ns-kw sub-vect]
  (update sub-vect 0 (partial ensure-ns ns-kw)))

(defn subscribe
  [query]
  (let [sub (->> query
                 ensure-vec
                 (ns-first :dhex.subs)
                 (rf/subscribe))]
    (when-not sub (throw (js/Error. "Unknown query" query)))
    (deref sub)))

(reg-sub
 ::active-page
 (fn [db _]
   (:active-page db)))

(reg-sub
 ::user
 (fn [db _]
   (:user db)))

(reg-sub
 ::profile
 (fn [db _]
   (:profile db)))

(reg-sub
 ::loading
 (fn [db]
   (:loading db)))

;; Loading functions defined

(reg-sub
 ::loading-articles?

 :<- [::loading]

 (fn [loading?]
   (:articles loading?)))

(reg-sub
 ::loading-article?

 :<- [::loading]

 (fn [loading?]
   (:article loading?)))

(reg-sub
 ::loading-tags?

 :<- [::loading]

 (fn [loading?]
   (:tags loading?)))

(reg-sub
 ::loading-login-user?

 :<- [::loading]

 (fn [loading?]
   (:login-user loading?)))

(reg-sub
 ::loading-update-user?

 :<- [::loading]

 (fn [loading?]
   (:update-user loading?)))

(reg-sub
 ::articles

 (fn [db]
   (->>
    (:articles db)
    (vals)
    (sort-by :epoch reverse-comp))))

(reg-sub
 ::comments

 (fn [db _]
   (->> (:comments db)
        (vals)
        (sort-by :epoch reverse-comp))))

(reg-sub
 ::active-article
 (fn [db]
   (get-in db [:articles (:active-article db)])))

(reg-sub
 ::articles-count
 (fn [db]
   (:articles-count db)))

(reg-sub
 ::tags

 (fn [db]
   (:tags db)))

(reg-sub
 ::filter

 (fn [db]
   (-> db
       :filter)))

(reg-sub
 ::tag

 :<- [::filter]

 (fn [filter]
   (:tag filter)))

(reg-sub
 ::offset

 :<- [::filter]

 (fn [filter]
   (:offset filter)))

(reg-sub
 ::author

 :<- [::filter]

 (fn [filter]
   (:author filter)))

(reg-sub
 ::favorites

 :<- [::filter]

 (fn [filter]
   (:favorites filter)))

;; Error Subscriptions
(reg-sub
 ::errors

 (fn [db]
   (:errors db)))

(reg-sub
 ::get-feed-articles-error

 :<- [::errors]

 (fn [errors]
   (:get-feed-articles errors)))

(reg-sub
 ::get-tags-error

 :<- [::errors]

 (fn [errors]
   (:get-tags errors)))
