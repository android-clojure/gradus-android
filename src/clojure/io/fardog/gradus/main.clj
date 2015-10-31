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
    (:import [android.app Activity]))

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
             :on-click (fn [_] (launch-settings activity))}]])

(defactivity io.fardog.gradus.MainActivity
  :key :main

  (onCreate [this bundle]
    (.superOnCreate this bundle)
    (neko.debug/keep-screen-on this)
    (on-ui
      (set-content-view! (*a) (get-main-layout (*a)))
      (if (empty? (:api-key @prefs))
        (launch-settings (*a))))))
