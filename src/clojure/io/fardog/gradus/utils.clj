(ns io.fardog.gradus.utils
  (:require [clj-http.lite.client :as client]
            [clojure.core.strint :refer [<<]]
            [neko.data.shared-prefs :refer [defpreferences]])
  (:import [android.content Context]
           [android.net ConnectivityManager NetworkInfo]
           [android.os AsyncTask]
           android.widget.TextView))

(defpreferences prefs "gradus_preferences")

(def url-template
    (str "https://api.wordnik.com/v4/~{resource}.json/"
         "~{query}/~{section}?api_key=~{api-key}"))

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
  [query]
  (if (not check-network!)
    (throw (Exception. "no network connection"))
    (let [resource  "word"
          section   "definitions"
          api-key   (:api-key @prefs)
          url       (<< "https://api.wordnik.com/v4/~{resource}.json/"
                        "~{query}/~{section}?api_key=~{api-key}")]
      (future
        (client/get url {:accept :json})))))
