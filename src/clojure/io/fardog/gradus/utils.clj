(ns io.fardog.gradus.utils
  (:require [clj-http.lite.client :as client]
            [clojure.data.json :as json]
            [neko.ui :as ui]
            [neko.ui.mapping :refer [defelement add-trait!]]
            [neko.ui.menu :as menu]
            [neko.ui.traits :as traits :refer [deftrait]]
            [neko.data.shared-prefs :refer [defpreferences]])
  (:import [android.content Context]
           [android.net ConnectivityManager NetworkInfo]
           [android.os AsyncTask]
           [android.view View]
           [android.support.v4.view ViewCompat]
           [android.support.v4.widget DrawerLayout DrawerLayout$DrawerListener]
           [android.support.v7.app AppCompatActivity ActionBarDrawerToggle]
           android.widget.TextView
           java.util.HashMap))

(defpreferences prefs "gradus_preferences")

(def url-template
    (str "https://api.wordnik.com/v4/%s.json/"
         "%s/%s?api_key=%s"))

(defn text-from-widget
  "Given a TextView element, get its text content"
  [^TextView text]
  (str (.getText text)))

(defn check-network!
  [activity]
  (let [^ConnectivityManager conn-mgr (.getSystemService activity Context/CONNECTIVITY_SERVICE)
        ^NetworkInfo net-info (.getActiveNetworkInfo conn-mgr)]
    (and net-info (.isConnected net-info))))

(defn http-get!
  "Perform a GET request to a given HTTP url, optionally with headers, defaults
  to JSON. Returns a future which resolves with a ring-like response, which
  will be JSON decoded by default.
  Throws an exception when no network connection is present."
  ([url]
   (http-get! url {:accept :json}))
  ([url headers]
   (if (not check-network!)
     (throw (Exception. "no network connection"))
     (future
       (let [response (client/get url headers)]
         (if (= :json (:accept headers))
           (assoc response :body (json/read-str (:body response)
                                                :key-fn keyword))
           response))))))

(defn wordnik-get!
  "Perform a query against the wordnik API.
  Returns a future which resolves with a ring-like response.
  Throws an exception when no network connection is present."
  ([query]
   (wordnik-get! query "definitions"))
  ([query section]
   (wordnik-get! query section "word"))
  ([query section resource]
   (let [api-key   (:api-key @prefs)
         url (format url-template resource query section api-key)]
     (http-get! url))))

;; drawer-layout from foreclosure-android
;; https://github.com/alexander-yakushev/foreclojure-android/
(defelement :drawer-layout
  :classname android.support.v4.widget.DrawerLayout
  :inherits :view-group
  :traits [:drawer-toggle])

(defelement :navigation-view
  :classname android.support.design.widget.NavigationView
  :inherits :frame-layout
  :traits [:navbar-menu :navbar-header-view])

(deftrait :drawer-layout-params
  "docs"
  {:attributes (concat (deref #'neko.ui.traits/margin-attributes)
                       [:layout-width :layout-height
                        :layout-weight :layout-gravity])
   :applies? (= container-type :drawer-layout)}
  [^View wdg, {:keys [layout-width layout-height layout-weight layout-gravity]
               :as attributes}
   {:keys [container-type]}]
  (let [^int width (->> (or layout-width :wrap)
                        (neko.ui.mapping/value :layout-params)
                        (neko.ui.traits/to-dimension (.getContext wdg)))
        ^int height (->> (or layout-height :wrap)
                         (neko.ui.mapping/value :layout-params)
                         (neko.ui.traits/to-dimension (.getContext wdg)))
        weight (or layout-weight 0)
        params (android.support.v4.widget.DrawerLayout$LayoutParams. width height weight)]
    (#'neko.ui.traits/apply-margins-to-layout-params (.getContext wdg) params attributes)
    (when layout-gravity
      (set! (. params gravity)
            (neko.ui.mapping/value :layout-params layout-gravity :gravity)))
    (.setLayoutParams wdg params)))

(deftrait :drawer-toggle
  "docs"
  {:attributes [:drawer-open-text :drawer-closed-text :drawer-indicator-enabled
                :on-drawer-closed :on-drawer-opened]}
  [^DrawerLayout wdg, {:keys [drawer-open-text drawer-closed-text
                              drawer-indicator-enabled
                              on-drawer-opened on-drawer-closed]}
   {:keys [^View id-holder]}]
  (let [toggle (proxy [ActionBarDrawerToggle DrawerLayout$DrawerListener]
                 [^android.app.Activity (.getContext wdg)
                  wdg
                  ^int (or drawer-open-text android.R$string/untitled)
                  ^int (or drawer-closed-text android.R$string/untitled)]
                 (onDrawerOpened [view]
                   (neko.-utils/call-if-nnil on-drawer-opened view))
                 (onDrawerClosed [view]
                   (neko.-utils/call-if-nnil on-drawer-closed view)))]
    (.setDrawerIndicatorEnabled toggle (boolean drawer-indicator-enabled))
    (.setDrawerListener wdg toggle)
    (when id-holder
      (.put ^HashMap (.getTag id-holder) :neko.ui/drawer-toggle toggle))))

(deftrait :navbar-header-view
  "docs "
  {:attributes [:header]}
  [^android.support.design.widget.NavigationView wdg, {:keys [header]} opts]
  (.addHeaderView wdg (ui/make-ui-element (.getContext wdg) header opts)))

(deftrait :navbar-menu
  "docs "
  {:attributes [:menu]}
  [^android.support.design.widget.NavigationView wdg, {:keys [menu]} _]
  (menu/make-menu (.getMenu wdg) menu))

(deftrait :elevation
  "docs "
  [^View wdg, {:keys [elevation]} _]
  (ViewCompat/setElevation wdg (neko.ui.traits/to-dimension
                                (.getContext wdg) elevation)))

(add-trait! :view :drawer-layout-params)
(add-trait! :view :elevation)
(add-trait! :linear-layout :drawer-layout-params)
