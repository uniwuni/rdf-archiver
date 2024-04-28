(ns uniwuni.videohandler
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string]
   [com.gfredericks.test.chuck.generators :as gen']
   [me.raynes.fs :as fs]
   [uniwuni.general :as general :refer [uri]]))

(s/def :uniwuni.video.youtube/id
  (let [regex #"[-_a-zA-Z0-9]{10}[048AEIMQUYcgkosw]"] (-> string?
                                                          (s/and #(re-matches regex %))
                                                          (s/with-gen (gen'/string-from-regex regex)))))
(s/def :uniwuni.video.youtube/title (s/and string? #(<= (count %) 100)))
(s/def :uniwuni.video.youtube/description (s/and string? #(<= (count %) 5000)))
(s/def :uniwuni.video.youtube/thumbnail (s/conformer uri :uniwuni/full-uri))
(s/def :uniwuni.video.youtube/channel-id
  (let [regex #"UC[0-9A-Za-z_-]{21}[AQgw]"] (-> string?
                                                (s/and #(re-matches regex %))
                                                (s/with-gen (gen'/string-from-regex regex)))))
(s/def :uniwuni.video.youtube/duration (s/and integer? #(<= 0 %)))
(s/def :uniwuni.video.youtube/channel (s/and string? #(<= (count %) 100)))
(s/def :uniwuni.video.youtube/language string?)

(s/def :uniwuni.video.youtube/upload-date-literal
  (let [regex #"\d{8}"] (-> string?
                            (s/and #(re-matches regex %))
                            (s/with-gen (gen'/string-from-regex regex)))))

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

(s/def :uniwuni.video.local.youtube/video (s/and fs/file? #(#{".mkv" ".webm" ".mp4"} (fs/extension %))))
(s/def :uniwuni.video.local.youtube/thumbnail (s/and fs/file? #(#{".webp" ".jpg"} (fs/extension %))))
(s/def :uniwuni.video.local.youtube/info (s/and fs/file? #(clojure.string/ends-with? (fs/base-name %) ".info.json")))
(s/def :uniwuni.video.local/youtube (s/keys :req [:uniwuni.video.local.youtube/video
                                                  :uniwuni.video.local.youtube/info]
                                            :opt [:uniwuni.video.local.youtube/thumbnail]))

(defn read-video-json [file]
  (with-open [r (io/reader file)]
    (json/read r :key-fn (comp keyword #(str "uniwuni.video.youtube/" %) #(clojure.string/replace % "_" "-")))))

(defn video-id->uri [id]
  (uri (str "https://youtu.be/" id)))

(s/fdef video-id->uri
  :args (s/cat :id :uniwuni.video.youtube/id)
  :ret :uniwuni/full-uri)

(defn channel-id->uri [id]
  (uri (str "https://www.youtube.com/channel/" id)))

(s/fdef channel-id->uri
  :args (s/cat :channel-id :uniwuni.video.youtube/channel-id)
  :ret :uniwuni/full-uri)

(defn video-path->local-youtube [path*]
  (let [path (fs/absolute path*)
        dir (fs/parent path)
        [base _] (fs/split-ext path)
        webp-thumbnail (fs/file dir (str base ".webp"))
        jpg-thumbnail (fs/file dir (str base ".jpg"))
        info-json (fs/file dir (str base ".info.json"))
        res (s/conform :uniwuni.video.local/youtube
                   (cond-> {:uniwuni.video.local.youtube/video path
                            :uniwuni.video.local.youtube/info info-json}
                     (fs/file? webp-thumbnail) (assoc :uniwuni.video.local.youtube/thumbnail webp-thumbnail)
                     (fs/file? jpg-thumbnail) (assoc :uniwuni.video.local.youtube/thumbnail jpg-thumbnail)))]
    (if (s/valid? :uniwuni.video.local/youtube res) res nil)))

(s/fdef video-path->local-youtube
  :args (s/cat :path fs/file?)
  :ret (s/nilable :uniwuni.video.local/youtube))

(defn handle-video [])
