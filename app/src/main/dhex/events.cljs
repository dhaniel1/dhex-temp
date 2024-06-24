(ns dhex.events
  (:require
   [cljs.reader :as rdr]
   [re-frame.core :as rf :refer [reg-event-db reg-event-fx reg-fx path after trim-v inject-cofx]]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax :refer [json-response-format json-request-format]]
   [dhex.db :as db :refer [set-user-ls remove-ls-user]]
   [dhex.api :as api :refer [endpoint]]))

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

(defn add-epoch
  [item]
  (assoc item :epoch (-> item :createdAt rdr/parse-timestamp .getTime)))

(defn index-by
  "The function is presumably a keyword"
  [f coll]
  (into {}
        (map (fn [item]
               (let [item (add-epoch item)]
                 [(f item) item])) coll)))

(reg-event-fx
 :set-active-page
 (fn [{:keys [db]} [_ {:keys [page slug profile favorited]}]]
   (let [set-page (assoc db :active-page page)]
     (case page
       ;; -- URL @ "/" --------------------------------------------------------
       :home {:db         set-page
              :dispatch-n [(if (empty? (:user db))
                             [:get-articles {:limit 10 :offset 0}]
                             [:get-feed-articles {:limit 10 :offset 0}])
                           [:get-tags]]}

       (:login :register :settings) {:db set-page} ;; `case` can group multiple clauses that do the same thing.

       :editor {:db       set-page
                :dispatch (if slug
                            [:set-active-article slug]
                            [:reset-active-article])}

       :article {:db         (assoc set-page :active-article slug)

                 :dispatch-n [[:get-articles {:offset 0 :limit 10}] ;; I THINK THIS SHOULD NOT BE CALLED
                              ;;[:get-articles {:offset 0 :limit 10}]
                              [:get-article-comments {:slug slug}]
                              (when-let [profile-var (get-in db [:articles slug :author :username])]
                                [:get-user-profile {:profile profile-var}])]}

;; -- URL @ "/profile/:slug" -------------------------------------------
       :profile {:db         (assoc set-page :active-article slug)

                 :dispatch-n [[:get-user-profile {:profile profile}]
                              [:get-articles {:offset 0 :author profile}]]}
       ;; -- URL @ "/profile/:slug/favorites" ---------------------------------
       :favorited {:db       (assoc db :active-page :profile)            ;; even though we are at :favorited, we still
                   :dispatch [:get-articles {:favorited favorited}]}))))

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
                 :on-failure      [:api-request-error {:request :get-feed-articles
                                                       :loading :articles}]}
    :db (-> db
            (assoc-in [:loading :articles] true)
            (assoc-in [:filter :offset] (:offset params))
            (assoc-in [:filter :tag] (:tag params))
            (assoc-in [:filter :author] (:author params))
            (assoc-in [:filter :favorites] (:favorited params))
            (assoc-in [:filter :feed] true))}))

(reg-event-db
 :get-feed-articles-success
 (fn [db [_ {:keys [articles] :as acc}]]
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
                 :headers        (auth-header db)
                 :timeout         5000
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:get-articles-success]
                 :on-failure      [:api-request-error {:request :get-articles
                                                       :loading :articles}]}
    :db (-> db
            (assoc-in [:loading :articles] true)
            (assoc-in [:filter :offset] (:offset params))
            (assoc-in [:filter :tag] (:tag params))
            (assoc-in [:filter :author] (:author params))
            (assoc-in [:filter :favorites] (:favorited params))
            (assoc-in [:filter :feed] false))}))

(reg-event-db
 :get-articles-success

 (fn [db [_ {:keys [articles] :as acc}]]

   (-> db
       (assoc-in [:loading :articles] false)
       (assoc :articles (index-by :slug articles)
              :articles-count  (:articlesCount acc)))))

;; (reg-event-fx
;;  :get-article
;;  (fn [{:keys [db]} [_ params]]
;;    {:http-xhrio {:method          :get
;;                  :uri             (endpoint "articles" (:slug params))
;;                  :params          params
;;                  :headers        (auth-header db) ;; This is used to authenticate the user
;;                  :timeout         5000
;;                  :response-format (json-response-format {:keywords? true})
;;                  :on-success      [:get-article-success]
;;                  :on-failure      [:api-request-error {:request :get-article
;;                                                        :loading :article}]}
;;     :db (assoc-in db [:loading :article] true)})

;;  (reg-event-db
;;   :get-article-success
;;   (fn [db [_ {:keys [article] :as acc}]]
;;     (println "Successfullly gets ARTICLE") ;; DELETE MEEEE
;;     (-> db
;;         (assoc-in [:loading :article] false)
;;         (assoc :articles (index-by :slug [article]))))))

(reg-event-fx
 :get-tags
 (fn [{:keys [db]}]

   {:db (assoc-in db [:loading :tags] true)

    :http-xhrio {:method          :get
                 :uri             (endpoint "tags")
                 :timeout         5000
                 :headers        (auth-header db)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:get-tags-success]
                 :on-failure      [:api-request-error {:request :get-tags
                                                       :loading :tags}]}}))

(reg-event-db
 :get-tags-success
 (fn [db [_ tagList]]
   (-> db
       (assoc-in [:loading :tags] false)
       (assoc :tags tagList))))

(reg-event-fx
 :get-tag
 (fn [{:keys [db]}]

   {:db (assoc-in db [:loading :tags] true)
    :http-xhrio {:method          :get
                 :uri             (endpoint "tags")
                 :timeout         5000
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:get-tags-success] ;; This should be tag not tags
                 :on-failure      [:api-request-error {:request :get-tag
                                                       :loading :tag}]}}))

;; (reg-event-db
;;  :get-tag-success

;;  (fn [db [_ tagList]]
;;    (-> db
;;        (assoc-in [:loading :tag] false)
;;        (assoc :tags tagList)))

(reg-event-fx
 :set-active-article

 (fn [{:keys [db]} [_ slug]]

   {:db         (assoc db :active-article slug)

    :dispatch-n [[:get-article-comments {:slug slug}]
                 [:get-user-profile {:profile (get-in db [:articles slug :author :username])}]]}))

(reg-event-fx
 :get-article-comments

 (fn [{:keys [db]} [_ params]]

   {:db         (assoc-in db [:loading :comments] true)

    :http-xhrio {:method          :get
                 :uri             (endpoint "articles" (:slug params) "comments") ;; evaluates to "api/articles/:slug/comments"
                 :headers         (auth-header db)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:get-article-comments-success]
                 :on-failure      [:api-request-error {:request :get-article-comments
                                                       :loading :comments}]}}))

(reg-event-db
 :get-article-comments-success
 (fn [db [_ {comments :comments}]]
   (-> db
       (assoc-in [:loading :comments] false)
       (assoc :comments (index-by :id comments)))))

(reg-event-fx
 :get-user-profile

 (fn [{:keys [db]} [_ params]]

   {:db         (assoc-in db [:loading :profile] true)

    :http-xhrio {:method          :get
                 :uri             (endpoint "profiles" (:profile params))
                 :headers         (auth-header db)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:get-user-profile-success]
                 :on-failure    [:api-request-error {:request :get-user-profile
                                                     :loading :profile}]}}))

(reg-event-db
 :get-user-profile-success

 (fn [db [_ {profile :profile}]]

   (-> db
       (assoc-in [:loading :profile] false)
       (assoc :profile profile))))

(reg-event-fx
 :register-user

 (fn [{:keys [db]} [_ credentials]]

   {:db (assoc-in db [:loading :register-user] true)

    :http-xhrio {:uri            (endpoint "users")
                 :method          :post
                 :params          {:user credentials}
                 :timeout         5000
                 :format          (json-request-format)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:register-user-success]
                 :on-failure     [:api-request-error {:request :register-user}]}}))

(reg-event-fx
 :register-user-success

 set-user-interceptor

 (fn [{:keys [db]} [{:keys [user]}]]

   {:db (-> (merge db user)
            (assoc-in [:loading :register-user] false))
    :dispatch [:navigate :home]}))

(reg-event-fx
 :login-user

 (fn [{:keys [db]} [_ credentials]]

   {:db (assoc-in db [:loading :login-user] true)

    :http-xhrio {:uri            (endpoint "users" "login")
                 :method          :post
                 :params          {:user credentials}
                 :timeout         5000
                 :format          (json-request-format)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:register-user-success]
                 :on-failure      [:api-request-error {:request :login-user}]}}))

(reg-event-fx
 :login-user-success

 set-user-interceptor

 (fn [{:keys [db]} [{:keys [user]}]]

   {:db (-> (merge db user)
            (assoc-in [:loading :loging] false))

    :dispatch [:navigate :home]}))

(reg-event-fx
 :update-user

 (fn [{:keys [db]} [_ credentials]]

   {:db (assoc-in db [:loading :update-user] true)

    :http-xhrio {:uri            (endpoint "user")
                 :method          :put
                ;;  :timeout         5000
                 :params          {:user credentials}
                 :headers         (auth-header db) ;; This is used to authenticate the user 
                 :format          (json-request-format)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:update-user-success]
                 :on-failure      [:api-request-error {:request :update-user}]}}))

(reg-event-fx
 :update-user-success

 set-user-interceptor

 (fn [{:keys [db]} [{:keys [user]}]]

   {:db (-> (update db :user merge user)
            (assoc-in [:loading :update-user] false))

    :dispatch [:navigate :home]})) ;; Do I need to navigate home?

(reg-event-fx
 :logout

 [(after remove-ls-user)]

 (fn [{:keys [db]}]

   {:db (dissoc db :user)
    :dispatch [:set-active-page {:page :home}]}))

(reg-event-fx
 :upsert-article

 (fn [{:keys [db]} [_ params]]

   {:db         (assoc-in db [:loading :article] true)

    :http-xhrio {:method          (if (:slug params) :put :post)
                 :uri             (if (:slug params)
                                    (endpoint "articles" (:slug params))
                                    (endpoint "articles"))
                 :headers         (auth-header db)
                 :params          {:article (:article params)}
                 :format          (json-request-format)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:upsert-article-success]
                 :on-failure      [:api-request-error {:request :upsert-article
                                                       :loading :article}]}}))

(reg-event-fx
 :upsert-article-success

 (fn [{:keys [db]} [_ {article :article}]]

   {:db         (-> db
                    (assoc-in [:loading :article] false)
                    (dissoc :comments)
                    (dissoc :errors)
                    (assoc :active-page :article
                           :active-article (:slug article)))
    :dispatch-n [[:get-article {:slug (:slug article)}]
                 [:get-article-comments {:slug (:slug article)}]]
    :set-url    {:url (str "/article/" (:slug article))}}))

(reg-event-fx
 :post-comment

 (fn [{:keys [db]} [_ body]]

   {:db         (assoc-in db [:loading :comments] true)

    :http-xhrio {:method          :post
                 :uri             (endpoint "articles" (:active-article db) "comments") ;; evaluates to "api/articles/:slug/comments"
                 :headers         (auth-header db)
                 :params          {:comment body}
                 :format          (json-request-format)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [:post-comment-success]
                 :on-failure      [:api-request-error {:request :post-comment
                                                       :loading :comments}]}}))

(reg-event-fx
 :post-comment-success

 (fn [{:keys [db]} [_ comment]]

   {:db       (-> db
                  (assoc-in [:loading :comments] false)
                  (assoc-in [:articles (:active-article db) :comments] comment)
                  (update :errors dissoc :comments)) ;; clean up errors, if any

    :dispatch [:get-article-comments {:slug (:active-article db)}]}))

(reg-event-db
 :api-request-error

 (fn [db [_ {:keys [request loading]} response]]

   (-> db
       (assoc-in [:errors request] (:debug-message response))
       (assoc-in [:loading (or loading request)] false))))
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;; Events to toggle fullscreen mode. yeah, it's very unrelated

(reg-fx
 :toggleFullScreen

 (fn []
   (let [doc js/document]
     (if (not (.-fullscreenElement doc))
       (-> doc
           .-documentElement
           .requestFullscreen)
       (when (.-exitFullscreen doc)
         (.exitFullscreen doc)))))

 #_(fn [_]
     (let [doc js/document]
       (if-not (.-fullscreenElement doc)
         (.requestFullscreen (.-documentElement doc))
         (.exitFullscreen doc)))))

(reg-event-fx
 :toggle-full-screen
 (fn [_]
   {:toggleFullScreen nil}))
