(ns io.fardog.gradus.detail
    (:require [io.fardog.gradus.utils :refer [check-network! wordnik-get!]]
              [clojure.pprint :refer [pprint]]
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
             [android.support.v7.app AppCompatActivity]
             [android.net ConnectivityManager NetworkInfo]
             [android.os AsyncTask]))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(defpreferences prefs "gradus_preferences")

(defn- get-item-layout
  [item]
  [:relative-layout {:padding-bottom [20 :dp]
                     :layout-width :match-parent}
   [:text-view {:id ::part-of-speech
                :text (str "(" (:partOfSpeech item "unknown") ")")
                :text-size [10 :dp]
                :text-color R$color/dim_purple}]
   [:text-view {:id ::text
                :layout-below ::part-of-speech
                :layout-width :fill-parent
                :text (:text item "")}]
   [:text-view {:id ::attribution
                :layout-below ::text
                :layout-align-right ::text
                :text-size [8 :dp]
                :text (:attributionText item "no attribution")}]])

(defn- get-detail-layout
  [activity query {:keys [status body]}]
  [:relative-layout {:padding [16 :dp]
                     :padding-bottom 0
                     :layout-width :match-parent}
   [:text-view {:id ::query
                :text-size [45 :sp]
                :text-color R$color/type_dark
                :text query}]
   [:scroll-view {:layout-width :fill-parent
                  :layout-height :wrap-content
                  :layout-below ::query}
    (into [:linear-layout {:id ::details
                           :orientation :vertical
                           :layout-width :match-parent}]
          (map get-item-layout body))]])

(defn- get-error-layout
  [activity error]
  [:linear-layout {:orientation :vertical}
   [:text-view {:text (str "An Error Occurred: " error)}]])

(defactivity io.fardog.gradus.DetailActivity
  :key :detail
  :extends AppCompatActivity

  (onCreate [this bundle]
    (.superOnCreate this bundle)
    (neko.debug/keep-screen-on this)

    (on-ui
      ; set up the action bar
      (.setDisplayHomeAsUpEnabled (.getSupportActionBar this) true)
      (.setHomeButtonEnabled (.getSupportActionBar this) true)
      (.setTitle (.getSupportActionBar this) "Definition")
      ; try to load our view
      (try
        (let [extras   (.. this getIntent getExtras)
              query    (:query (like-map extras))
              response (wordnik-get! query)]
          (set-content-view! (*a) (get-detail-layout (*a) query @response)))
        (catch Exception e
          (set-content-view! (*a) (get-error-layout (*a) e)))))))
