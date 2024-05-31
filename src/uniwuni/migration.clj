(ns uniwuni.migration
  (:require
   [clojure.java.shell]
   [clojure.string :as str]
   [uniwuni.config :as config]
   [uniwuni.general :as general :refer [uri]]
   [uniwuni.videohandler :as videohandler]
   [me.raynes.fs :as fs]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]))

(defn is-video [path*]
  (let [path (fs/absolute path*)
        dir (fs/parent path)
        [base ext] (fs/split-ext path)
        info-json (fs/file dir (str base ".info.json"))]
    (and (fs/file? path)
         (contains? #{".mkv" ".webm" ".mp4"} ext)
         (fs/file? info-json)
         (= \[ (first (take-last 13 base)))) ; make sure its not a twitter video or somethiung
    ))


(defn get-all-videos []
  (filter is-video
          (file-seq (:uniwuni.config.platform/folder (:uniwuni.config.platform/youtube (:uniwuni.config/platforms uniwuni.config/config)))))
  )

(defn add-all-videos [& flags]
  (pmap #(videohandler/handle-video % {}) (get-all-videos))
  )
