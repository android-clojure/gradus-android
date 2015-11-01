(ns io.fardog.gradus.utils
  (:require [clj-http.lite.client :as client]
            [clojure.data.json :as json]
            [neko.data.shared-prefs :refer [defpreferences]])
  (:import [android.content Context]
           [android.net ConnectivityManager NetworkInfo]
           [android.os AsyncTask]
           android.widget.TextView))

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
           (assoc response :body (json/read-str (:body response)))
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
