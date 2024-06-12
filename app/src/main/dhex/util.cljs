(ns dhex.util
  (:require [clojure.string :as string :refer [join split]]))

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

(defn split-at-dot
  [string]
  (-> string
      (split #"\.")
      first
      (str ".")
      (string/capitalize)))
