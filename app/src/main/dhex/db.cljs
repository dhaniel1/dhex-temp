(ns dhex.db
  (:require
   [cljs.reader :as reader]
   [re-frame.core :as rf :refer [reg-cofx]]))

(def default-db
  {:active-page :home
   :user {}}) ;; implement an interceptor that does the check for user path

(def dhex-user-key "dhex-user")

(defn set-user-ls
  [user-cred]
  (.setItem js/localStorage dhex-user-key (str user-cred)))

(reg-cofx
 :ls-user
 (fn [cofx _]
   (assoc cofx :ls-user
          (into (sorted-map) (some->> (.getItem js/localStorage dhex-user-key)
                                      (reader/read-string))))))

(defn remove-ls-user
  []
  (.removeItem js/localStorage dhex-user-key))
