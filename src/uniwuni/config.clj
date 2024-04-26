(ns uniwuni.config)

(require '[outpace.config :refer [defconfig]])

(defconfig folder-youtube "Archive/Videos/Youtube History/ytvideos")
(defconfig folder-soundcloud "Archive/Musik/Soundcloud Likes/")
(defconfig endpoint-query "http://localhost:3030/archive/")
(defconfig endpoint-update "http://localhost:3030/archive/update/")
(defconfig mountpoint-archive "/run/media/uni/Neoproterozoikum")
(defconfig archive-host "http://localhost:9999/")
