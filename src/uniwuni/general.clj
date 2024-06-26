(ns uniwuni.general
  (:require [com.yetanalytics.flint :as f]
            [com.yetanalytics.flint.spec
             [prologue :as f.s.prologue]
             [axiom :as f.s.axiom]
             [query :as f.s.query]]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [ont-app.vocabulary.core :as voc]
            [clojure.string :as str]))

(defn uri-safe-char? [c] (or (Character/isLetterOrDigit c) (some #(= c %) "-_!.")))

(s/fdef uri-safe-char?
  :args (s/cat :c char?)
  :ret boolean?)

(defn make-uri-safe [s] (apply str (map #(if (uri-safe-char? %) % "_") s)))

(s/fdef make-uri-safe
  :args (s/cat :s string?)
  :ret string?)

(def iri-gen
  (gen/one-of (list (s/gen uri?)
                    (gen/let [x gen/string-alphanumeric] (str "unia:" "word" x "word")))))

(s/def :uniwuni/file #(instance? java.io.File %))
(s/def :uniwuni/absolute-file (s/and :uniwuni/file #(.isAbsolute %)))

(s/def :uniwuni/uri (s/with-gen f.s.axiom/iri-or-prefixed-spec (constantly iri-gen)))
(s/def :uniwuni/full-uri uri?)

(s/def :uniwuni.account/url :uniwuni/uri)
(s/def :uniwuni.account/name string?)
(s/def :uniwuni.account/platform :uniwuni/uri)

(s/def :uniwuni/account (s/keys :req [:uniwuni.account/url
                                      :uniwuni.account/name
                                      :uniwuni.account/platform]))

(defn uri [s] (new java.net.URI (str s)))
(s/fdef uri
  :args (s/cat :str string?)
  :ret :uniwuni/full-uri)

(defn escape-for-sparql [s]
  (str/escape s {\\ "\\\\" \" "\\\"" \newline "\\n" \return "\\r" })
  )
