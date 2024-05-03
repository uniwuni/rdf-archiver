(ns uniwuni.videohandler
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string]
   [com.gfredericks.test.chuck.generators :as gen']
   [me.raynes.fs :as fs]
   [com.yetanalytics.flint :as f]
   [ont-app.sparql-endpoint.core :as spq]
   [uniwuni.general :as general :refer [uri]]
   [uniwuni.queries :as queries]
   [uniwuni.config :as config]
   [clojure.spec.test.alpha :as stest]
   [ont-app.vocabulary.core :as voc]))

;(defmulti get-uri :type)
;(defmethod get-uri :wah [x] "wah")
;
;
;
;

(defn video-id->uri "youtu.be URI from video id" [id]
  (uri (str "https://youtu.be/" id)))

(s/fdef video-id->uri
  :args (s/cat :id :uniwuni.video.youtube/id)
  :ret :uniwuni/full-uri)

(defn channel-id->uri "YouTube channel URI from channel id" [id]
  (uri (str "https://www.youtube.com/channel/" id)))

(s/fdef channel-id->uri
  :args (s/cat :channel-id :uniwuni.video.youtube/channel-id)
  :ret :uniwuni/full-uri)

(defn path->host-uri [path*]
  (let [parent (((config/config :uniwuni.config/platforms) :uniwuni.config.platform/youtube) :uniwuni.config.platform/folder)
        path (fs/absolute path*)
        prefix (((config/config :uniwuni.config/platforms) :uniwuni.config.platform/youtube) :uniwuni.config.platform/prefix)
        ; insane hack: in order to avoid url encoding ourselves while avoiding the slashes,
        ; we resolve the path against /, such that we get an absolute path that can be turned into an uri
        ; without any pwd-dependencies. this uri we turn into a string and remove the file prefix
        quasipath (.resolve (.toPath (fs/file "/")) (.relativize (.toPath parent) (.toPath path)))]
    (->> quasipath
         .toFile
         .toURI
         str
         (#(subs % 6))
         (str prefix)
         uri)))

(s/fdef path->host-uri
  :args (s/cat :file fs/file?)
  :ret :voc/uri-str-spec)

(voc/register-resource-type-context! :uniwuni.video.youtube/resource-type-context ::voc/resource-type-context)

(defmethod voc/resource-type [:uniwuni.video.youtube/resource-type-context clojure.lang.IPersistentMap] [map] (map :type))
(defn video->name [video]
  (general/make-uri-safe (str (:uniwuni.video.youtube/title video) "-" (:uniwuni.video.youtube/id video))))

(s/fdef video->name
  :args (s/cat :video :uniwuni.video/youtube)
  :ret string?)

(defn video-resource->uri [infix video] {:post [(s/assert :voc/uri-str-spec %)]}
  (uri (str (config/config :uniwuni.config/prefix-archive) infix (video->name video))))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources/uploader [video]
  (or (:uniwuni.video.youtube/uploader video) (uri (str (config/config :uniwuni.config/prefix-archive) "agents/youtube/" (general/make-uri-safe (:uniwuni.video.youtube/channel video)) "-" (:uniwuni.video.youtube/channel-id video)))))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources/channel [video]
  (channel-id->uri (:uniwuni.video.youtube/channel-id video)))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources/work [video]
  (video-resource->uri "biblio/work/" video))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources/expression [video]
  (video-resource->uri "biblio/expr/" video))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources/manifestation-remote [video]
  (video-id->uri (:uniwuni.video.youtube/id video)))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources/manifestation-file [video]
  (video-resource->uri "biblio/mani/file/" video))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources/item [video]
  (path->host-uri (:uniwuni.video.local.youtube/video video)))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources.thumbnail/work [video]
  (video-resource->uri "biblio/work/thumbnail-" video))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources.thumbnail/expression [video]
  (video-resource->uri "biblio/expr/thumbnail-" video))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources.thumbnail/manifestation-remote [video]
  (:uniwuni.video.youtube/thumbnail video))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources.thumbnail/manifestation-file [video]
  (video-resource->uri "biblio/mani/file/thumbnail-" video))

(defmethod voc/as-uri-string :uniwuni.video.youtube.resources.thumbnail/item [video]
  (path->host-uri (:uniwuni.video.local.youtube/thumbnail video)))

(defn video-data->uri [video type] (voc/as-uri-string (assoc video :type type)))

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

(defn parse-youtube-date-literal "Convert YYYYMMDD literals to proper LocalDateTimes" [date-str]
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
; note that videos can contain thumbnails thus make for valid thumbnail files
(s/def :uniwuni.video.local.youtube/thumbnail (s/and fs/file? #(#{".webp" ".jpg" ".mkv" ".webm" ".mp4"} (fs/extension %))))
(s/def :uniwuni.video.local.youtube/info (s/and fs/file? #(clojure.string/ends-with? (fs/base-name %) ".info.json")))
(s/def :uniwuni.video.local/youtube (s/keys :req [:uniwuni.video.local.youtube/video
                                                  :uniwuni.video.local.youtube/info]
                                            :opt [:uniwuni.video.local.youtube/thumbnail]))

(defn read-video-json "Read yt-dlp formatted .info.json file about a youtube video and conforms it into usable shape"
  [file]
  (with-open [r (io/reader file)]
    (s/conform :uniwuni.video/youtube
               (json/read r :key-fn (comp keyword #(str "uniwuni.video.youtube/" %) #(clojure.string/replace % "_" "-"))))))

(s/fdef read-video-json
  :args (s/cat :file fs/file?)
  :ret :uniwuni.video/youtube)

(defn video-path->local-youtube "Info about local video from video file" [path*]
  (let [path (fs/absolute path*)
        dir (fs/parent path)
        [base _] (fs/split-ext path)
        webp-thumbnail (fs/file dir (str base ".webp"))
        jpg-thumbnail (fs/file dir (str base ".jpg"))
        info-json (fs/file dir (str base ".info.json"))
        res (cond-> {:uniwuni.video.local.youtube/video path
                     :uniwuni.video.local.youtube/info info-json}
              true (assoc :uniwuni.video.local.youtube/thumbnail path) ; thumbnail is stored in video
              (fs/file? webp-thumbnail) (assoc :uniwuni.video.local.youtube/thumbnail webp-thumbnail)
              (fs/file? jpg-thumbnail) (assoc :uniwuni.video.local.youtube/thumbnail jpg-thumbnail))]
    (if (s/valid? :uniwuni.video.local/youtube res)
      (s/conform :uniwuni.video.local/youtube res)
      (s/explain :uniwuni.video.local/youtube res))))

(s/fdef video-path->local-youtube
  :args (s/cat :path fs/file?)
  :ret (s/nilable :uniwuni.video.local/youtube))

(defn get-maybe-add-agents [video]
  (let [query-endpoint (-> config/config :uniwuni.config/sparql :uniwuni.config.sparql/query)
        update-endpoint (-> config/config :uniwuni.config/sparql :uniwuni.config.sparql/update)
        agents (fn [] (-> video
                          (assoc :type :uniwuni.video.youtube.resources/channel)
                          voc/as-uri-string
                          queries/agent-of-channel?-query
                          (queries/exec-select query-endpoint)
                          (#(map :agent %))))
        agents1 (agents)
        update-agents (fn [] (let
                              [agent-url (video-data->uri video :uniwuni.video.youtube.resources/uploader)
                               account (video-data->uri video :uniwuni.video.youtube.resources/channel)
                               agent-name (:uniwuni.video.youtube/channel video)
                               account-data {:uniwuni.account/url account
                                             :uniwuni.account/name agent-name
                                             :uniwuni.account/platform (uri "https://www.youtube.com/")}
                               update (queries/add-agent-account!-update agent-url account-data)]
                               (queries/exec-updates! update update-endpoint)))]
    (if (empty? agents1)
      (do (update-agents) (agents))
      agents1)))

(defn video-add-query [video]
  (list {:prefixes queries/my-prefixes
         :insert-data
         [(merge {(video-data->uri video :uniwuni.video.youtube.resources/work)
                  {:a #{:frbr/Work :fabio/MovingImage}
                   :dce/title #{(:uniwuni.video.youtube/title video)}
                   :frbr/realization #{(video-data->uri video :uniwuni.video.youtube.resources/expression)}}

                  (video-data->uri video :uniwuni.video.youtube.resources/expression)
                  (merge {:a #{:frbr/Expression :fabio/Movie}
                          :frbr/embodiment
                          #{(video-data->uri video :uniwuni.video.youtube.resources/manifestation-remote)
                            (video-data->uri video :uniwuni.video.youtube.resources/manifestation-file)}
                          :dce/title #{(:uniwuni.video.youtube/title video)}}
                         (if (:uniwuni.video.youtube/language video) {:dce/language #{(:uniwuni.video.youtube/language video)}} {}))

                  (video-data->uri video :uniwuni.video.youtube.resources/manifestation-remote)
                  {:a #{:frbr/Manifestation :fabio/WebManifestation}
                   :frbr/producer #{(video-data->uri video :uniwuni.video.youtube.resources/uploader)}
                   :frbr/reproduction #{(video-data->uri video :uniwuni.video.youtube.resources/manifestation-file)}
                   :dce/date #{(:uniwuni.video.youtube/upload-date video)}
                   :dce/title #{(:uniwuni.video.youtube/title video)}
                   :dce/description #{(:uniwuni.video.youtube/description video)}
                   :dce/extent #{(:uniwuni.video.youtube/duration video)}}

                  (video-data->uri video :uniwuni.video.youtube.resources/manifestation-file)
                  {:a #{:frbr/Manifestation :fabio/DigitalManifestation}
                   :frbr/reproduction #{(video-data->uri video :uniwuni.video.youtube.resources/manifestation-file)}
                   :frbr/exemplar #{(video-data->uri video :uniwuni.video.youtube.resources/item)}
                   :dce/date #{(:uniwuni.video.youtube/epoch video)}
                   :dce/title #{(:uniwuni.video.youtube/title video)}
                   :dce/description #{(:uniwuni.video.youtube/description video)}
                   :dce/extent #{(:uniwuni.video.youtube/duration video)}}

                  (video-data->uri video :uniwuni.video.youtube.resources/item)
                  {:a #{:frbr/Item :fabio/ComputerFile}
                   :frbr/owner :unic/me}

                  (video-data->uri video :uniwuni.video.youtube.resources.thumbnail/work)
                  {:a #{:frbr/Work :fabio/StillImage}
                   :frbr/realization #{(video-data->uri video :uniwuni.video.youtube.resources.thumbnail/expression)}}

                  (video-data->uri video :uniwuni.video.youtube.resources.thumbnail/expression)
                  {:a #{:frbr/Expression :fabio/Cover}
                   :frbr/complement #{(video-data->uri video :uniwuni.video.youtube.resources/expression)}
                   :frbr/embodiment
                   #{(video-data->uri video :uniwuni.video.youtube.resources.thumbnail/manifestation-remote)
                     (video-data->uri video :uniwuni.video.youtube.resources.thumbnail/manifestation-file)}}

                  (video-data->uri video :uniwuni.video.youtube.resources.thumbnail/manifestation-remote)
                  {:a #{:frbr/Manifestation :fabio/WebManifestation}
                   :frbr/producer #{(video-data->uri video :uniwuni.video.youtube.resources/uploader)}
                   :frbr/reproduction #{(video-data->uri video :uniwuni.video.youtube.resources.thumbnail/manifestation-file)}
                   :dce/date #{(:uniwuni.video.youtube/upload-date video)}}

                  (video-data->uri video :uniwuni.video.youtube.resources.thumbnail/manifestation-file)
                  {:a #{:frbr/Manifestation :fabio/DigitalManifestation}
                   :frbr/exemplar #{(video-data->uri video :uniwuni.video.youtube.resources.thumbnail/item)}
                   :dce/date #{(:uniwuni.video.youtube/epoch video)}}}

                 {(video-data->uri video :uniwuni.video.youtube.resources.thumbnail/item)
                  {:a #{:frbr/Item :fabio/ComputerFile}
                   :frbr/owner #{:unic/me}}})]}))

(defn handle-data-video [video opts]
  (let [query-endpoint (-> config/config :uniwuni.config/sparql :uniwuni.config.sparql/query)
        update-endpoint (-> config/config :uniwuni.config/sparql :uniwuni.config.sparql/update)]
    (when (or (:overwrite opts) (not (queries/exec-ask (queries/is-embodied?-query (video-data->uri video :uniwuni.video.youtube.resources/manifestation-remote)) query-endpoint)))
      (let [agents (get-maybe-add-agents video)
            video2 (assoc video :uniwuni.video.youtube/uploader (first agents))] ; arbitrary choice but idc
        (queries/exec-updates! (video-add-query video2) update-endpoint)))))

(defn video-path->youtube [path]
  (let [local-video (video-path->local-youtube path)
        video (merge local-video (read-video-json (:uniwuni.video.local.youtube/info local-video)))]
    video))

(defn handle-video [path opts]
  (handle-data-video (video-path->youtube path) opts))

;(stest/instrument)
