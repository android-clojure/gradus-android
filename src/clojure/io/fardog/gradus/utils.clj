(ns io.fardog.gradus.utils
  (:require [clj-http.lite.client :as client]
            [clojure.core.strint :refer [<<]]
            [neko.data.shared-prefs :refer [defpreferences]])
  (:import [android.content Context]
           [android.net ConnectivityManager NetworkInfo]
           [android.os AsyncTask]))

(defpreferences prefs "gradus_preferences")

(def url-template
    (str "https://api.wordnik.com/v4/~{resource}.json/"
         "~{query}/~{section}?api_key=~{api-key}"))

(defn check-network!
  [activity]
  (let [^ConnectivityManager conn-mgr (.getSystemService activity Context/CONNECTIVITY_SERVICE)
        ^NetworkInfo net-info (.getActiveNetworkInfo conn-mgr)]
    (and net-info (.isConnected net-info))))

;; (defn- get-download-task
;;   [url]
;;   (proxy [AsyncTask] []
;;     (doInBackground [urls]
;;       (try
;;         (

(defn http-get!
  []
  (if (not check-network!)
    (throw (Exception. "No Network Connection")))
  (let [resource  "word"
        section   "definitions"
        query     "beep"
        api-key   (:api-key @prefs)]
    ;;(client/get "http://site.com/resources/3" {:accept :json})
    (<< "https://api.wordnik.com/v4/~{resource}.json/"
        "~{query}/~{section}?api_key=~{api-key}")))