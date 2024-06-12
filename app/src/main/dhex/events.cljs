(ns dhex.events
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-event-fx reg-fx path after trim-v inject-cofx]]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax :refer [json-response-format json-request-format]]
   [dhex.db :as db :refer [set-user-ls]]
   [dhex.api :as api :refer [endpoint endpoint-direct]]))

;; Interceptors 
(def set-user-interceptor [(path :user)
                           (after set-user-ls)
                           trim-v])

(reg-event-fx
 :initialize-db

 [(inject-cofx :ls-user)]

 (fn  [{:keys [ls-user]} _]
   {:db (assoc db/default-db :user ls-user)}))

(reg-event-fx
 :navigate
 (fn  [_ [_ handler & args]]
   {:navigate [handler args]}))

(defn auth-header
  [db]
  (when-let [token (get-in db [:user :token])]
    {:Authorization  (str "Token " token)}))

#_(reg-event-fx
   :set-active-panel
   (fn  [{:keys [db]} [_ active-panel]]
     {:db (assoc db :active-panel active-panel)}))

(reg-event-fx
 :get-feed-articles
 (fn [{:keys [db]} [_ params]]
   {:http-xhrio {:method          :get
                 :uri             (endpoint "articles" "feed")
                 :params          params
                 :headers         (auth-header db) ;; This is used to authenticate the user
                 :timeout         5000
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:get-feed-articles-success]
                 :on-failure      [:logger]}
    :db (-> db
            (assoc-in [:loading :articles] true)
            (assoc-in [:filter :offset] (:offset params)) ;; base on passed params set a filter
            (assoc-in [:filter :tag] (:tag params)) ;; so that we can easily show and hide
            (assoc-in [:filter :author] (:author params)) ;; appropriate ui components
            (assoc-in [:filter :favorites] (:favorited params))
            (assoc-in [:filter :feed] true))}))

(reg-event-db
 :get-feed-articles-success
 (fn [db [_ {:keys [articles articlesCount] :as acc}]]
   (println "Feed Articles Successfulllll") ;; DELETE MEEEE
   (-> db
       (assoc-in [:loading :articles] false)
       (assoc :articles articles
              :articles-count  (:articlesCount acc)))))

(reg-event-fx
 :get-articles
 (fn [{:keys [db]} [_ params]]
   {:http-xhrio {:method          :get
                 :uri             (endpoint "articles")
                 :params          params
                 :headers        (auth-header db) ;; This is used to authenticate the user
                 :timeout         5000
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:get-articles-success]
                 :on-failure      [:logger]}
    :db (-> db
            (assoc-in [:loading :articles] true)
            (assoc-in [:filter :offset] (:offset params)) ;; base on passed params set a filter
            (assoc-in [:filter :tag] (:tag params)) ;; so that we can easily show and hide
            (assoc-in [:filter :author] (:author params)) ;; appropriate ui components
            (assoc-in [:filter :favorites] (:favorited params))
            (assoc-in [:filter :feed] false))}))

(reg-event-db
 :get-articles-success
 (fn [db [_ {:keys [articles articlesCount] :as acc}]]
   (println "Successfulllll") ;; DELETE MEEEE
   (-> db
       (assoc-in [:loading :articles] false)
       (assoc :articles articles
              :articles-count  (:articlesCount acc)))))

(reg-event-fx
 :get-tags
 (fn [{:keys [db]}]

   {:http-xhrio {:method          :get
                 :uri             (endpoint "tags")
                 :timeout         5000
                 :headers        (auth-header db) ;; This is used to authenticate the user
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:get-tags-success]
                 :on-failure      [:logger]}
    :db (assoc-in db [:loading :tags] true)}))

(reg-event-db
 :get-tags-success
 (fn [db [_ tagList]]
   (-> db
       (assoc-in [:loading :tags] false)
       (assoc :tags tagList))))

(reg-event-fx
 :get-tag
 (fn [{:keys [db]}]

   {:http-xhrio {:method          :get
                 :uri             (endpoint "tags")
                 :timeout         5000
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:get-tags-success]
                 :on-failure      [:logger]}
    :db (assoc-in db [:loading :tags] true)}))

(reg-event-fx
 :logger
 (fn [_ [_ result]]
   (println result)))

(reg-event-fx
 :set-active-page
 (fn [{:keys [db]} [_ {:keys [page slug profile favorited]}]]
   (let [set-page (assoc db :active-page page)]
     (case page
       ;; -- URL @ "/" --------------------------------------------------------
       :home {:db         set-page
              :dispatch-n [(if (empty? (:user db)) ;; dispatch more than one event.
                             [:get-articles {:limit 10 :offset 0}] ;; When a user is NOT logged in, display all articles,
                             [:get-feed-articles {:limit 10 :offset 0}]) ;; otherwise get her/his feed articles.
                           [:get-tags]]}

;; -- URL @ "/login" | "/register" | "/settings" -----------------------
       (:login :register :settings) {:db set-page} ;; `case` can group multiple clauses that do the same thing.
                                                   ;; ie., `(:login :register :settings) {:db set-page}` is the same as
                                                   ;;      ```                                                   ;;      :login {:db set-page}
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

(reg-event-fx
 :register-user
 (fn [{:keys [db]} [_ credentials]]

   {:db (assoc-in db [:loading :registering] true)

    :http-xhrio {:uri            (endpoint "users")
                 :method          :post
                 :params          {:user credentials}
                 :timeout         5000
                 :format          (json-request-format)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:register-user-success]
                 :on-failure      [:logger]}}))

(reg-event-fx
 :register-user-success

 set-user-interceptor

 (fn [{:keys [db]} [{:keys [user]}]]
   {:db (-> (merge db user)
            (assoc-in [:loading :registering] false))

    :dispatch [:navigate :home]}))

(reg-event-fx
 :login-user
 (fn [{:keys [db]} [_ credentials]]

   {:db (assoc-in db [:loading :loging] true)

    :http-xhrio {:uri            (endpoint "users" "login")
                 :method          :post
                 :params          {:user credentials}
                 :timeout         5000
                 :format          (json-request-format)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:register-user-success]
                 :on-failure      [:logger]}}))

(reg-event-fx
 :login-user-success

 set-user-interceptor

 (fn [{:keys [db]} [{:keys [user]}]]
   {:db (-> (merge db user)
            (assoc-in [:loading :loging] false))

    :dispatch [:navigate :home]}))
