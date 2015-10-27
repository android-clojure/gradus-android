(ns io.fardog.gradus.detail
    (:require [io.fardog.gradus.utils :refer [check-network! http-get!]]
              [neko.action-bar :refer [setup-action-bar]]
              [neko.activity :refer [defactivity set-content-view!]]
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
  [activity]
  [:linear-layout {:orientation :vertical}
   [:text-view {:text "Details"}]
   [:button {:text "Check Network"
             :on-click (fn
                         [_]
                         (if (check-network! activity)
                           (toast (http-get!) :long)
                           (toast "not connected :(" :long)))}]])

(defactivity io.fardog.gradus.DetailActivity
  :key :detail

  (onCreate [this bundle]
    (.superOnCreate this bundle)
    (neko.debug/keep-screen-on this)
    (on-ui
      (set-content-view! (*a) (get-detail-layout (*a))))))
