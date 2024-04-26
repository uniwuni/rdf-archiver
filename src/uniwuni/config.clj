(ns uniwuni.config
  (:require
    [clojure.spec.alpha :as s]
    [clojure.java.io :as io]))

(require '[uniwuni.general :as general :refer [uri]])

(s/def :uniwuni.config.platform/folder :uniwuni/absolute-file)
(s/def :uniwuni.config.platform/prefix :uniwuni/full-uri)

(s/def :uniwuni.config/platform (s/keys :req [:uniwuni.config.platform/folder
                                             :uniwuni.config.platform/prefix]))


(s/def :uniwuni.config.platform/youtube :uniwuni.config/platform)
(s/def :uniwuni.config.platform/soundcloud :uniwuni.config/platform)
(s/def :uniwuni.config/platforms (s/keys :req [:uniwuni.config.platform/youtube
                                               :uniwuni.config.platform/soundcloud]))

(s/def :uniwuni.config.sparql/query :uniwuni/full-uri)
(s/def :uniwuni.config.sparql/update :uniwuni/full-uri)
(s/def :uniwuni.config/sparql (s/keys :req [:uniwuni.config.sparql/query
                                            :uniwuni.config.sparql/update]))

(s/def :uniwuni.config/prefix-archive :uniwuni/full-uri)

(s/def :uniwuni/config (s/keys :req [:uniwuni.config/platforms
                                     :uniwuni.config/sparql
                                     :uniwuni.config/prefix-archive]))

(def default-config
  {:uniwuni.config/platforms
   {:uniwuni.config.platform/youtube
    {:uniwuni.config.platform/folder (io/file "/run/media/uni/Neoproterozoikum/Archive/Videos/Youtube History/ytvideos")
     :uniwuni.config.platform/prefix (uri "http://localhost:9999/Archive/Videos/Youtube%20History/ytvideos/")}
    :uniwuni.config.platform/soundcloud
    {:uniwuni.config.platform/folder (io/file "/run/media/uni/Neoproterozoikum/Archive/Musik/Soundcloud Likes/")
     :uniwuni.config.platform/prefix (uri "http://localhost:9999/Archive/Musik/Soundcloud%20Likes/")}}
   :uniwuni.config/sparql
   {:uniwuni.config.sparql/query (uri "http://localhost:3030/archive/")
    :uniwuni.config.sparql/update (uri "http://localhost:3030/archive/update/")}
   :uniwuni.config/prefix-archive (uri "https://uniwuni.github.io/archives#")})


(s/assert :uniwuni/config default-config)

;; TODO: make config configurable
(def config default-config)

(s/assert :uniwuni/config config)
