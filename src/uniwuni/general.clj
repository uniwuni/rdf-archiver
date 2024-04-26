(ns uniwuni.general)
(require '[com.yetanalytics.flint :as f])
(require '[com.yetanalytics.flint.spec
           [prologue :as f.s.prologue]
           [axiom :as f.s.axiom]
           [query :as f.s.query]])
(require '[clojure.spec.alpha :as s])
(require '[clojure.test.check.generators :as gen])

(def iri-gen
    (gen/one-of (list (s/gen uri?)
                 (gen/let [x gen/string-alphanumeric] (str "unia:" "word" x "word")))))


(s/def :uniwuni/uri (s/with-gen f.s.axiom/iri-or-prefixed-spec (constantly iri-gen)))

(s/def :uniwuni.account/url :uniwuni/uri)
(s/def :uniwuni.account/name string?)
(s/def :uniwuni.account/platform :uniwuni/uri)

(s/def :uniwuni/account (s/keys :req [:uniwuni.account/url
                                      :uniwuni.account/name
                                      :uniwuni.account/platform]))
