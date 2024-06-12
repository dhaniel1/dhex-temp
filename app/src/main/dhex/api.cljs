(ns dhex.api
  (:require [clojure.string :as string :refer [join]]))

(def api-url "https://api.realworld.io/api")
(def localhost "http://localhost:4000")

(defn endpoint
  "concatinates api-url with / "
 [& args]
 (->> (cons localhost args) 
     (join "/")))


(defn endpoint-direct
  "concatinates api-url with / "
 [& args]
 (->> (cons api-url args) 
     (join "/")))

