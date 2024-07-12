(ns uniwuni.downloader
  (:require
   [clojure.java.shell]
   [clojure.string :as str]
   [uniwuni.config :as config]
   [uniwuni.general :as general :refer [uri]]
   [uniwuni.videohandler :as videohandler]
   [me.raynes.fs :as fs]
   [clojure.tools.logging :as log]))

(defn get-playlist-entries [playlist limit]
  (let [_ (log/info "Getting playlist entries for" playlist)
        res
        (clojure.java.shell/sh
         "yt-dlp"
         "--flat-playlist"
         (str playlist)
         "--cookies" (str (:uniwuni.config.platform.youtube/cookie-file (:uniwuni.config.platform/youtube (:uniwuni.config/platforms uniwuni.config/config))))
         "--print" "%(url)s"
         "--playlist-end" (str limit))]
    (if (= 0 (:exit res))
      (map uri (str/split-lines (:out res)))
      (do (log/fatal "yt-dlp failed" (:err res)) (throw (ex-info "yt-dlp failed" res))))))

(defn download-video [video]
  (let [_ (log/info "Downloading" video)
        res
        (clojure.java.shell/sh
         "yt-dlp"
         (str video)
         "--download-archive" (str (:uniwuni.config.platform.youtube/archive-file (:uniwuni.config.platform/youtube (:uniwuni.config/platforms uniwuni.config/config))))
         "-f" "bestvideo[height<=720]+bestaudio/best[height<=720]"
         "--embed-metadata"
         "--write-subs"
         "--embed-subs"
         "--write-auto-subs"
         "--sub-langs" "'en.*,de.*'"
         "--embed-thumbnail"
         "--xattrs"
         "--write-info-json"
         "--embed-info-json"
         "--embed-chapters"
         "--sub-format" "'ass/srt/best'"
         "--print" "after_move:filepath"
         :dir  (:uniwuni.config.platform/folder (:uniwuni.config.platform/youtube (:uniwuni.config/platforms uniwuni.config/config))))]
    (if (= 0 (:exit res))
      (if (empty? (:out res))
        (do (log/info "Video" video "already archived")
            :already-added)
        (do (log/info "Video" video "downloaded")
            (fs/file (str/trim (:out res)))))

      (throw (ex-info "yt-dlp failed" res)))))

(defn download-and-handle-video [video opts]
  (let [res (download-video video)]
    (when (not= :already-added res)
      (videohandler/handle-video res opts))))

(defn download-playlist [playlist limit]
  (let [entries (get-playlist-entries playlist limit)]
    (map #(try (download-video %) (catch Exception e (do (log/warn "Exception: " (.getMessage e)) :error))) entries)))



(defn download-and-handle-playlist [playlist limit opts]
  (pmap #(videohandler/handle-video % opts)
       (filter #(and (not= :error %) (not= :already-added %)) (download-playlist playlist limit))))

(defn handle-recent [& args] (download-and-handle-playlist (uri "https://www.youtube.com/feed/history") 100 {}))
