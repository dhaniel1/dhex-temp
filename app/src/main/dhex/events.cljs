(ns dhex.events
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-event-fx reg-cofx reg-fx]]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax :refer [json-response-format json-request-format]]
   [dhex.db :as db]))

(reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(reg-event-fx
 :navigate
 (fn  [_ [_ handler]]
   {:navigate handler}))

(reg-event-fx
 :set-active-panel
 (fn  [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)}))

(reg-event-fx
 :http-call
 (fn []
   {:http-xhrio {:method          :get
                 :uri             "http://localhost:4000/"
                ; :params          data
                 :timeout         5000
                ; :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:logger]
                 :on-failure      [:logger]}}))

(reg-event-fx
 :logger
 (fn [_ [_ result]]
   (println result)))

(reg-event-fx                                            ;; usage: (dispatch [:set-active-page {:page :home})
 :set-active-page                                        ;; triggered when the user clicks on a link that redirects to another page
 (fn [{:keys [db]} [_ {:keys [page slug profile favorited]}]] ;; destructure 2nd parameter to obtain keys
   (let [set-page (assoc db :active-page page)]
     (case page
       ;; -- URL @ "/" --------------------------------------------------------
       :home {:db         set-page
              :dispatch-n [(if (empty? (:user db)) ;; dispatch more than one event.
                             [:logger "working"]
 [:logger "working"]
                            ; [:get-articles {:limit 10}] ;; When a user is NOT logged in, display all articles,
                            ; [:get-feed-articles {:limit 10}]
) ;; otherwise get her/his feed articles.
                         ;  [:get-tags]
]}          ;; we also can't forget to get tags

       ;; -- URL @ "/login" | "/register" | "/settings" -----------------------
       (:login :register :settings) {:db set-page} ;; `case` can group multiple clauses that do the same thing.
                                                   ;; ie., `(:login :register :settings) {:db set-page}` is the same as
                                                   ;;      ```
                                                   ;;      :login {:db set-page}
                                                   ;;      :register {:db set-page}
                                                   ;;      :settings {:db set-page}
                                                   ;;      ```
       ;; -- URL @ "/editor" --------------------------------------------------
       :editor {:db       set-page
                :dispatch (if slug                     ;; When we click article to edit we need
                            [:set-active-article slug] ;; to set it active or if we want to write
                            [:reset-active-article])}  ;; a new article we reset

       ;; -- URL @ "/article/:slug" -------------------------------------------
       :article {:db         (assoc set-page :active-article slug)
                 ;; :dispatch-n to dispatch multiple events
                 :dispatch-n [[:get-articles {:limit 10}]
                              [:get-article-comments {:slug slug}]
                              [:get-user-profile {:profile (get-in db [:articles slug :author :username])}]]}

       ;; -- URL @ "/profile/:slug" -------------------------------------------
       :profile {:db         (assoc set-page :active-article slug)
                 ;; :dispatch-n to dispatch multiple events
                 :dispatch-n [[:get-user-profile {:profile profile}]
                              [:get-articles {:author profile}]]}
       ;; -- URL @ "/profile/:slug/favorites" ---------------------------------
       :favorited {:db       (assoc db :active-page :profile)            ;; even though we are at :favorited, we still
                   :dispatch [:get-articles {:favorited favorited}]})))) ;; display :profile with :favorited articles
