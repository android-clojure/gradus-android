(ns io.fardog.gradus.main
    (:require [io.fardog.gradus.utils :refer [text-from-widget]]
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
             [android.support.v7.app AppCompatActivity]))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(defpreferences prefs "gradus_preferences")

(defn- do-query
  [activity]
  (let [query (text-from-widget (find-view activity ::query-input))
        detail (intent "io.fardog.gradus.DETAIL" {:query query})]
    (.startActivity activity detail)))

(defn- launch-settings
  [activity]
  (let [settings (intent "io.fardog.gradus.SETTINGS" {})]
    (.startActivity activity settings)))


(defn- get-main-layout
  [activity]
  [:drawer-layout {:id ::drawer
                   :drawer-indicator-enabled true}
   [:linear-layout {:orientation :vertical}
    [:linear-layout {:orientation :horizontal
                     :layout-width :match-parent}
     [:edit-text {:id ::query-input
                  :layout-weight 1
                  :layout-width 0
                  :hint R$string/query_input}]
     [:button {:text R$string/query_button
               :layout-width :wrap-content
               :on-click (fn [_] (do-query activity))}]]
    [:button {:text "Settings"
              :on-click (fn [_] (launch-settings activity))}]]
   [:navigation-view {:id ::navbar
                      :layout-width [200 :dp]
                      :layout-height :fill
                      :layout-gravity :left
                      :menu [[:item {:title "Settings"
                                     :icon R$drawable/ic_launcher
                                     :show-as-action [:always :with-text]
                                     :on-click (fn [_] (launch-settings activity))}]]}]])

(defn refresh-ui [^Activity activity]
  (.syncState (find-view activity :neko.ui/drawer-toggle))
  (.invalidateOptionsMenu activity))

(defactivity io.fardog.gradus.MainActivity
  :key :main
  :extends AppCompatActivity

  (onCreate [this bundle]
    (.superOnCreate this bundle)
    (neko.debug/keep-screen-on this)
    (on-ui
      (.setDisplayHomeAsUpEnabled (.getSupportActionBar (*a)) true)
      (.setHomeButtonEnabled (.getSupportActionBar (*a)) true)

      (set-content-view! (*a) (get-main-layout (*a)))
      (refresh-ui (*a))
      (if (empty? (:api-key @prefs))
        (launch-settings (*a))))))
