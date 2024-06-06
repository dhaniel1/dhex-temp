(ns dhex.views.shared.footer
  (:require [dhex.util :as u]))

(defn footer
  []
  [:section.footer-section
   [:footer.app-footer.flex.items-center.justify-between.p-4.mx-auto {:class (str "w-11/12")}
    [:p.app-text-logo.text-2xl "DheX"]

    [:p.faded "Dhex \u00A9"
     [:span (u/current-year)
      " Simplicity is the ultimate sophistication"]]

    [:div.flex.gap-2
     [:div.logo-icon.twitter]
     [:div.logo-icon.telegram]
     [:div.logo-icon.whatsapp]]]])

