(ns uniwuni.queries
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [ont-app.vocabulary.core :as voc]
   [ont-app.igraph-vocabulary.core :as igv :refer [mint-kwi]]
   [com.yetanalytics.flint :as f]
   [com.yetanalytics.flint.spec
    [prologue :as f.s.prologue]
    [query :as f.s.query]
    [update :as f.s.update]]
   [uniwuni.general :as general :refer [uri]]
   [uniwuni.config :as config :refer [config]]
   [ont-app.sparql-endpoint.core :as spq]))

(s/check-asserts true)

(def my-prefixes
  {:owl (uri "http://www.w3.org/2002/07/owl#")
   :rdf (uri "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
   :rdfs (uri "http://www.w3.org/2000/01/rdf-schema#")
   :xsd  (uri "http://www.w3.org/2001/XMLSchema#")
   :foaf (uri "http://xmlns.com/foaf/0.1/")
   :frbr (uri "http://purl.org/vocab/frbr/core#")
   :dce (uri "http://purl.org/dc/elements/1.1/")
   :dct (uri "http://purl.org/dc/terms/")
   :unic (uri "https://uniwuni.github.io/me#")
   :unia (config :uniwuni.config/prefix-archive)
   :fabio (uri "http://purl.org/spar/fabio/")
   :mo (uri "http://purl.org/ontology/mo/")})

(s/assert ::f.s.prologue/prefixes my-prefixes)

(voc/put-ns-meta!
 'uniwuni.archive
 {:vann/preferredNamespacePrefix "unia"
  :vann/preferredNamespaceUri (my-prefixes :unia)})

(defmethod voc/resource-type java.net.URI [_] :voc/UriString)
(defmethod voc/as-uri-string java.net.URI [uri] (str uri)) ;throw
(defn agent-of-channel?-query [account-url]
  {:prefixes my-prefixes
   :select ['?agent]
   :where [{'?agent {:a #{:foaf/Agent}
                     :foaf/account #{account-url}}}]})

(s/fdef agent-of-channel?-query
  :args (s/cat :account-url :uniwuni/uri)
  :ret f.s.query/select-query-spec)

(defn is-embodied?-query [manifestation-url]
  {:prefixes my-prefixes
   :ask []
   :where [{'?video {:a #{:frbr/Expression}
                     :frbr/embodiment #{manifestation-url}}}]})

(s/fdef is-embodied?-query
  :args (s/cat :account-url :uniwuni/uri)
  :ret f.s.query/ask-query-spec)

(defn add-account!-update [agent-url account-data]
  [{:prefixes my-prefixes
    :insert-data [[[agent-url :foaf/name (account-data :uniwuni.account/name)]
                   [agent-url :foaf/account (account-data :uniwuni.account/url)]
                   [(account-data :uniwuni.account/url) :a :foaf/Account]
                   [(account-data :uniwuni.account/url) :foaf/accountServiceHomepage (account-data :uniwuni.account/platform)]
                   [(account-data :uniwuni.account/url) :foaf/accountName (account-data :uniwuni.account/name)]]]}])

(s/fdef add-account!-update
  :args (s/cat :agent-url :uniwuni/uri :account-data :uniwuni/account)
  :ret (s/coll-of f.s.update/insert-data-update-spec))

(defn add-agent-account!-update [agent-url account-data]
  (cons
   {:prefixes my-prefixes
    :insert-data [agent-url :a :foaf/Agent]}
   (add-account!-update agent-url account-data)))

(s/fdef add-agent-account!-update
  :args (s/cat :agent-url :uniwuni/uri :account-data :uniwuni/account)
  :ret (s/coll-of f.s.update/insert-data-update-spec))

(def simplify (spq/make-simplifier (spq/update-translators spq/default-translators
                                       :uri uri)))

(defn exec-select [query query-endpoint]
  (->> query
      (f/format-query)
      (spq/sparql-select query-endpoint)
      (map simplify)))

(defn exec-updates! [queries update-endpoint]
  (->> queries
      (f/format-updates)
      (spq/sparql-update update-endpoint)))

(stest/instrument)
