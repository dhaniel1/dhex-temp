(ns dhex.subs
  (:require
   [re-frame.core :as rf :refer [reg-sub]]
   [dhex.util :as u]))

(defn subscribe
  [query]
  (let [sub (->> query
                 u/ensure-vec
                 (u/ns-first :dhex.subs)
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
 ::articles

 (fn [db]
   (:articles db)))

(reg-sub
::articles-count
(fn [db]
  ( :articles-count db)))

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
