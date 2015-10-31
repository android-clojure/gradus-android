(ns io.fardog.gradus.settings
    (:require [io.fardog.gradus.utils :refer [text-from-widget]]
              [neko.action-bar :refer [setup-action-bar]]
              [neko.activity :refer [defactivity set-content-view!]]
              [neko.data.shared-prefs :refer [defpreferences]]
              [neko.debug :refer [*a]]
              [neko.notify :refer [toast]]
              [neko.resource :as res]
              [neko.find-view :refer [find-view]]
              [neko.threading :refer [on-ui]])
    (:import android.widget.EditText 
             [android.content Intent]))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(defpreferences prefs "gradus_preferences")

(defn- update-pref
  "Update the given preference"
  [pref value]
  (swap! prefs assoc pref value))

(defn- save-api-key
  [activity]
  (let [api-key (text-from-widget (find-view activity ::apikey-input))]
    (if (empty? api-key)
      (toast (res/get-string R$string/api_key_message_empty) :long)
      (update-pref :api-key api-key))))

(defn- get-settings-layout 
  [activity]
  [:linear-layout {:orientation :vertical}
   [:linear-layout {:orientation :horizontal
                    :layout-width :match-parent}
    [:edit-text {:id ::apikey-input
                 :hint R$string/api_key_input
                 :text (:api-key @prefs "")
                 :layout-width 0
                 :layout-weight 1}]
    [:button {:text R$string/api_key_save_button
              :on-click (fn [_] (save-api-key activity))}]]])

;; This is how an Activity is defined. We create one and specify its onCreate
;; method. Inside we create a user interface that consists of an edit and a
;; button. We also give set callback to the button.
(defactivity io.fardog.gradus.SettingsActivity
  :key :settings

  (onCreate [this bundle]
    (.superOnCreate this bundle)
    (neko.debug/keep-screen-on this)
    (on-ui
      (set-content-view! (*a) (get-settings-layout (*a))))))
