(ns uniwuni.videohandler
    (:require
    [clojure.spec.alpha :as s]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [clojure.string]
    [com.gfredericks.test.chuck.generators :as gen']))

(require '[uniwuni.general :as general :refer [uri]])

(s/def :uniwuni.video.youtube/id
  (let [regex #"[-_a-zA-Z0-9]{10}[048AEIMQUYcgkosw]"] (s/with-gen (s/and string? #(re-matches regex %)) (gen'/string-from-regex regex))))
(s/def :uniwuni.video.youtube/title (s/and string? #(<= (count %) 100)))
(s/def :uniwuni.video.youtube/description (s/and string? #(<= (count %) 5000)))
(s/def :uniwuni.video.youtube/thumbnail (s/conformer uri :uniwuni/full-uri))
(s/def :uniwuni.video.youtube/channel-id
  (let [regex #"UC[0-9A-Za-z_-]{21}[AQgw]"] (s/with-gen (s/and string? #(re-matches regex %)) (gen'/string-from-regex regex))))
(s/def :uniwuni.video.youtube/duration (s/and integer? #(<= 0 %)))
(s/def :uniwuni.video.youtube/channel (s/and string? #(<= (count %) 100)))
(s/def :uniwuni.video.youtube/language string?)

(s/def :uniwuni.video.youtube/upload-date-literal
    (let [regex #"\d{8}"] (s/with-gen (s/and string? #(re-matches regex %)) (gen'/string-from-regex regex))))

;; Define a conformer to convert valid date literals to inst
(defn parse-youtube-date-literal [date-str]
  (let [year (Integer/parseInt (subs date-str 0 4))
        month (Integer/parseInt (subs date-str 4 6))
        day (Integer/parseInt (subs date-str 6 8))]
    (.atStartOfDay (java.time.LocalDate/of year month day))))

(s/def :uniwuni.video.youtube/upload-date (s/and :uniwuni.video.youtube/upload-date-literal
                                                 (s/conformer parse-youtube-date-literal)
                                                 #(instance? java.time.LocalDateTime %)))
(s/def :uniwuni.video.youtube/epoch (s/and (s/conformer #(java.time.Instant/ofEpochSecond %))
                                           inst?))

(s/def :uniwuni.video/youtube
  (s/keys :req [:uniwuni.video.youtube/id
               :uniwuni.video.youtube/title
               :uniwuni.video.youtube/description
               :uniwuni.video.youtube/thumbnail
               :uniwuni.video.youtube/channel-id
               :uniwuni.video.youtube/duration
               :uniwuni.video.youtube/channel
               :uniwuni.video.youtube/upload-date
               :uniwuni.video.youtube/epoch] :opt [:uniwuni.video.youtube/language]))

(defn read-video-json [file]
    (with-open [r (io/reader file)]
        (json/read r :key-fn (comp keyword #(str "uniwuni.video.youtube/" %) #(clojure.string/replace % "_" "-")))))


(defn id->short-link [id]
    (uri (str "https://youtu.be/" id)))

(s/fdef id->short-link
    :args (s/cat :id :uniwuni.video.youtube/id)
    :ret :uniwuni/full-uri)


(defn handle-video [])
