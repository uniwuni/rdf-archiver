(ns uniwuni.videohandler-test
  (:require [uniwuni.videohandler :as sv]
            [uniwuni.general :as sg :refer [uri]]
            [uniwuni.general-test :as gt]
            [clojure.java.io :as io]
            [clojure.test :refer [testing is deftest]]
            [me.raynes.fs :as fs]
            [clojure.spec.alpha :as s]))


; temporary
(def profile identity)

(deftest read-video-json-test
  (testing "Reading video JSON"
    (let [video (sv/read-video-json (io/resource "Teeza - The Scorpion.info.json"))]
      (is (= (uri "https://youtu.be/egMjYeZXXI8")
             (sv/video-id->uri (:uniwuni.video.youtube/id video))) "Shortlink should match")
      (is (= "Teeza - The Scorpion" (:uniwuni.video.youtube/title video)) "Title should match")
      (is (= "Solomans Archive" (:uniwuni.video.youtube/channel video)) "Channel should match")
      (is (= "UCOQdqxbKazl4M_uM0HZrPiw" (:uniwuni.video.youtube/channel-id video)) "Channel should match")
      (is (= 267 (:uniwuni.video.youtube/duration video)) "Duration should match")
      (is (= nil (:uniwuni.video.youtube/language video)) "Language should not exist")
      (is (= (java.time.LocalDateTime/of 2024 04 28 0 0) (:uniwuni.video.youtube/upload-date video)) "Upload date should match")
      (is (= (inst-ms (:uniwuni.video.youtube/epoch video)) (* 1714304293 1000)) "Download date should match"))))

(deftest video-path->local-youtube-test
  (testing "Video path to local youtube"
    (let [videofile (io/resource "Teeza - The Scorpion.mkv")
          info (io/resource "Teeza - The Scorpion.info.json")
          thumb (io/resource "Teeza - The Scorpion.jpg")
          video (sv/video-path->local-youtube (fs/file videofile))]
          (is (= video {:uniwuni.video.local.youtube/video (fs/file videofile)
                        :uniwuni.video.local.youtube/info (fs/file info)
                        :uniwuni.video.local.youtube/thumbnail (fs/file thumb)}))))
  (testing "Video path to local youtube should not work")
        (let [videofile (io/resource "incomplete.webm")
          video (sv/video-path->local-youtube (fs/file videofile))]
          (is (nil? video))))

(deftest ->uri-test
  (testing "ID to link"
    (gt/test-spec `sv/video-id->uri)
    (gt/test-spec `sv/channel-id->uri)))
