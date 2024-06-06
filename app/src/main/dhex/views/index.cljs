(ns dhex.views.index
  (:require
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [dhex.routes :as routes]
   [dhex.subs :as subs]
   [dhex.views.shared.navbar :as navbar]
   [dhex.views.shared.footer :as footer]

   ;; These screens are imported here for now because it's the only way
   ;; to get it to the compiler

   [dhex.views.home]
   [dhex.views.register]))

;; main
(defn main-panel []
  (let [active-page (subscribe [::subs/active-page])
        _ (println "Value for active page is " @active-page)
        prepared-active-page  (-> @active-page
                                  name
                                  (str "-view")
                                  keyword)]
    [:div.container.mx-auto.h-full.flex.flex-col.relative.min-h-screen
     [navbar/navbar]  ;; global header section

     [:div.app-app-app
      (routes/panels prepared-active-page)]

     [footer/footer]   ;; global footer section
     ]))
