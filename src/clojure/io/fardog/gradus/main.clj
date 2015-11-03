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
             android.os.Build$VERSION
             [android.support.v7.app AppCompatActivity]
             [android.support.v4.widget DrawerLayout DrawerLayout$DrawerListener]
             [android.support.v7.app AppCompatActivity ActionBarDrawerToggle]))

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
               :on-click (fn [_] (do-query activity))}]]]
   [:navigation-view {:id ::navbar
                      :layout-width [200 :dp]
                      :layout-height :fill
                      :layout-gravity :left
                      :menu [[:item {:title "Settings"
                                     :icon android.R$drawable/ic_menu_preferences
                                     :show-as-action [:always :with-text]
                                     :on-click (fn [_] (launch-settings activity))}]
                             [:item {:title "About"
                                     :icon android.R$drawable/ic_menu_info_details
                                     :show-as-action [:always :with-text]
                                     :on-click (fn [_] (toast "TODO" :long))}]]}]])

(defn refresh-ui [^Activity activity]
  (.syncState (find-view activity :neko.ui/drawer-toggle))
  (.invalidateOptionsMenu activity))

(defactivity io.fardog.gradus.MainActivity
  :key :main
  :extends AppCompatActivity

  (onCreate [this bundle]
    (.superOnCreate this bundle)
    (neko.debug/keep-screen-on this)

    (when (>= Build$VERSION/SDK_INT 21)
      (.addFlags (.getWindow this)
                 android.view.WindowManager$LayoutParams/FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS))

    (on-ui
      (.setDisplayHomeAsUpEnabled (.getSupportActionBar (*a)) true)
      (.setHomeButtonEnabled (.getSupportActionBar (*a)) true)

      (set-content-view! (*a) (get-main-layout (*a)))
      (refresh-ui (*a))
      (if (empty? (:api-key @prefs))
        (launch-settings (*a)))))

  (onPostCreate [this bundle]
    (.superOnPostCreate this bundle)
    (.syncState (find-view this :neko.ui/drawer-toggle)))

  (onOptionsItemSelected [this item]
    false
    (if (.onOptionsItemSelected (find-view this :neko.ui/drawer-toggle) item)
      true
      (.superOnOptionsItemSelected this item))))


