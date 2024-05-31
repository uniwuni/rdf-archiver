(ns uniwuni.config
  (:require
   [clojure.spec.alpha :as s]
   [clojure.java.io :as io]
   [uniwuni.general :as general :refer [uri]]))

(s/def :uniwuni.config.platform/folder :uniwuni/absolute-file)
(s/def :uniwuni.config.platform/prefix :uniwuni/full-uri)
(s/def :uniwuni.config.platform.youtube/cookie-file :uniwuni/absolute-file)
(s/def :uniwuni.config.platform.youtube/archive-file :uniwuni/absolute-file)

(s/def :uniwuni.config/platform (s/keys :req [:uniwuni.config.platform/folder
                                              :uniwuni.config.platform/prefix]))
(s/def :uniwuni.config.platform/youtube (s/merge :uniwuni.config/platform
                                                 (s/keys :req [:uniwuni.config.platform.youtube/cookie-file
                                                               :uniwuni.config.platform.youtube/archive-file])))

(s/def :uniwuni.config.platform/soundcloud :uniwuni.config/platform)
(s/def :uniwuni.config/platforms (s/keys :req [:uniwuni.config.platform/youtube
                                               :uniwuni.config.platform/soundcloud]))

(s/def :uniwuni.config.sparql/query string?)
(s/def :uniwuni.config.sparql/update string?)
(s/def :uniwuni.config/sparql (s/keys :req [:uniwuni.config.sparql/query
                                            :uniwuni.config.sparql/update]))

(s/def :uniwuni.config/prefix-archive :uniwuni/full-uri)

(s/def :uniwuni/config (s/keys :req [:uniwuni.config/platforms
                                     :uniwuni.config/sparql
                                     :uniwuni.config/prefix-archive]))

(def default-config
  {:uniwuni.config/platforms
   {:uniwuni.config.platform/youtube
    {:uniwuni.config.platform/folder (io/file "/run/media/uni/Neoproterozoikum/Archive/Videos/YouTube History/ytvideos")
     :uniwuni.config.platform/prefix (uri "http://localhost:9999/Archive/Videos/YouTube%20History/ytvideos/")
     :uniwuni.config.platform.youtube/cookie-file (io/file "/run/media/uni/Neoproterozoikum/Archive/Videos/YouTube History/cookies.txt")
     :uniwuni.config.platform.youtube/archive-file (io/file "/run/media/uni/Neoproterozoikum/Archive/Videos/YouTube History/ytvideos/.archive.txt")
     }
    :uniwuni.config.platform/soundcloud
    {:uniwuni.config.platform/folder (io/file "/run/media/uni/Neoproterozoikum/Archive/Musik/Soundcloud Likes/")
     :uniwuni.config.platform/prefix (uri "http://localhost:9999/Archive/Musik/Soundcloud%20Likes/")}}
   :uniwuni.config/sparql
   {:uniwuni.config.sparql/query  "http://localhost:3030/archive_rewrite/sparql"
    :uniwuni.config.sparql/update "http://localhost:3030/archive_rewrite/update"} ;change for test
   :uniwuni.config/prefix-archive (uri "https://uniwuni.github.io/archives/")})

(s/assert :uniwuni/config default-config)

;; TODO: make config configurable
(def config default-config)

(s/assert :uniwuni/config config)
