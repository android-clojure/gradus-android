(ns io.fardog.gradus.settings
    (:require [neko.activity :refer [defactivity set-content-view!]]
              [neko.data.shared-prefs :refer [defpreferences]]
              [neko.debug :refer [*a]]
              [neko.notify :refer [toast]]
              [neko.resource :as res]
              [neko.find-view :refer [find-view]]
              [neko.threading :refer [on-ui]])
    (:import android.widget.EditText 
             android.widget.TextView))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(defpreferences prefs "gradus_preferences")

(defn text-from-widget
  "Given a TextView element, get its text content"
  [^TextView text]
  (str (.getText text)))

(defn- update-and-call
  "Update the given preference, and call a function when done"
  [pref value f]
  (swap! prefs assoc pref value)
  (toast (str (name pref) " saved") :long)
  (f))

(defn- save-api-key
  [activity]
  (let [api-key (text-from-widget (find-view activity ::apikey-input))]
    (if (empty? api-key)
      (toast (res/get-string R$string/api_key_message_empty) :long)
      (update-and-call :api-key api-key #()))))

(def settings-layout [:linear-layout {:orientation :vertical}
                  [:edit-text {:id ::apikey-input
                               :hint R$string/api_key_input
                               :text (:api-key @prefs "")
                               :layout-width :fill}]
                  [:button {:text R$string/api_key_save_button
                            :on-click (fn [_] (save-api-key (*a)))}]])

;; This is how an Activity is defined. We create one and specify its onCreate
;; method. Inside we create a user interface that consists of an edit and a
;; button. We also give set callback to the button.
(defactivity io.fardog.gradus.SettingsActivity
  :key :settings

  (onCreate [this bundle]
    (.superOnCreate this bundle)
    (neko.debug/keep-screen-on this)
    (on-ui
      (set-content-view! (*a) settings-layout))))
