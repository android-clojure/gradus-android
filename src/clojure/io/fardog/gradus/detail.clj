(ns io.fardog.gradus.detail
    (:require [io.fardog.gradus.utils :refer [check-network! http-get!]]
              [clojure.pprint :refer [pprint]]
              [neko.action-bar :refer [setup-action-bar]]
              [neko.activity :refer [defactivity set-content-view!]]
              [neko.data :refer [like-map]]
              [neko.data.shared-prefs :refer [defpreferences]]
              [neko.debug :refer [*a]]
              [neko.intent :refer [intent]]
              [neko.notify :refer [toast]]
              [neko.resource :as res]
              [neko.find-view :refer [find-view]]
              [neko.threading :refer [on-ui]])
    (:import [android.app Activity]
             [android.net ConnectivityManager NetworkInfo]
             [android.os AsyncTask]))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(defpreferences prefs "gradus_preferences")

(defn- get-detail-layout
  [activity query {:keys [status body]}]
  [:linear-layout {:orientation :vertical}
   [:text-view {:text query}]
   [:text-view {:text body}]])

(defn- get-error-layout
  [activity error]
  [:linear-layout {:orientation :vertical}
   [:text-view {:text (str "An Error Occurred: " error)}]])

(defactivity io.fardog.gradus.DetailActivity
  :key :detail

  (onCreate [this bundle]
    (.superOnCreate this bundle)
    (neko.debug/keep-screen-on this)
    (on-ui
      (try
        (let [extras   (.. this getIntent getExtras)
              query    (:query (like-map extras))
              response (http-get! query)]
          (set-content-view! (*a) (get-detail-layout (*a) query @response)))
        (catch Exception e
          (set-content-view! (*a) (get-error-layout (*a) e)))))))
