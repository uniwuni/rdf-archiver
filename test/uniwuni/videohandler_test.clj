(ns uniwuni.videohandler-test
  (:require [uniwuni.videohandler :as sv]
            [uniwuni.general :as sg]
            [uniwuni.general-test :as gt]
            [clojure.test :as t]
            [clojure.java.io :as io] ))

(require '[com.yetanalytics.flint.spec
           [prologue :as f.s.prologue]
           [axiom :as f.s.axiom]
           [query :as f.s.query]]
           '[clojure.test :refer [testing is deftest]])
(require '[clojure.test.check.generators :as gen])

(require '[clojure.spec.alpha :as s])
(require '[clojure.spec.test.alpha :as stest])
(require '[uniwuni.general :as general :refer [uri]])

(deftest read-video-json-test
  (testing "Reading video JSON"
    (let [video* (sv/read-video-json (io/resource "Teeza - The Scorpion.info.json"))
          video (s/conform :uniwuni.video/youtube video*)]

      (t/is (s/valid? :uniwuni.video/youtube video*) "Video should validate")
      (t/is (= (uri "https://youtu.be/egMjYeZXXI8")
               (sv/video-id->uri (:uniwuni.video.youtube/id video))) "Shortlink should match")
      (t/is (= "Teeza - The Scorpion" (:uniwuni.video.youtube/title video)) "Title should match")
      (t/is (= "Solomans Archive" (:uniwuni.video.youtube/channel video)) "Channel should match")
      (t/is (= "UCOQdqxbKazl4M_uM0HZrPiw" (:uniwuni.video.youtube/channel-id video)) "Channel should match")
      (t/is (= 267 (:uniwuni.video.youtube/duration video)) "Duration should match")
      (t/is (= nil (:uniwuni.video.youtube/language video)) "Language should not exist")
      (t/is (= (java.time.LocalDateTime/of 2024 04 28 0 0) (:uniwuni.video.youtube/upload-date video)) "Upload date should match")
      (t/is (= (inst-ms (:uniwuni.video.youtube/epoch video)) (* 1714304293 1000)) "Download date should match")
      )))

(deftest ->uri-test
  (testing "ID to link"
    (gt/test-spec `sv/video-id->uri)
    (gt/test-spec `sv/channel-id->uri)))
