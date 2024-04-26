(ns uniwuni.queries
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]))
(require '[com.yetanalytics.flint :as f])
(require '[com.yetanalytics.flint.spec
           [prologue :as f.s.prologue]
           [axiom :as f.s.axiom]
           [query :as f.s.query]])
(require '[clojure.spec.alpha :as s])
(require '[clojure.test.check.generators :as gen])

(s/check-asserts true)

(def unia-prefix "https://uniwuni.github.io/archives#")

(def my-prefixes
  {:owl "<http://www.w3.org/2002/07/owl#>"
   :rdf "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
   :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
   :xsd  "<http://www.w3.org/2001/XMLSchema#>"
   :foaf "<http://xmlns.com/foaf/0.1/>"
   :frbr "<http://purl.org/vocab/frbr/core#>"
   :dce "<http://purl.org/dc/elements/1.1/>"
   :dct "<http://purl.org/dc/terms/>"
   :unic "<https://uniwuni.github.io/me#>"
   :unia (str "<" unia-prefix ">")
   :fabio "<http://purl.org/spar/fabio/>"
   :mo "<http://purl.org/ontology/mo/>"})

(s/assert ::f.s.prologue/prefixes my-prefixes)

(defn agent-of-channel?-query [account-url]
  {:prefixes my-prefixes
    :select ['?agent]
    :where [{'?agent {:a #{:foaf/Agent}
                      :foaf/account #{account-url}}}]})

(s/fdef agent-of-channel?-query
  :args (s/cat :account-url f.s.axiom/iri-or-prefixed-spec)
  :ret f.s.query/select-query-spec)

(defn is-embodied?-query [manifestation-url]
  {:prefixes my-prefixes
   :ask []
   :where [{'?video {:a #{:frbr/Expression}
                     :frbr/embodiment #{manifestation-url}}}]})

(s/fdef is-embodied?-query
  :args (s/cat :account-url f.s.axiom/iri-or-prefixed-spec)
  :ret f.s.query/ask-query-spec)

(defn add-account!-update [agent-url account-data]
  [{:prefixes my-prefixes
   :insert-data [[[agent-url :foaf/name (account-data :account/name)]
                  [agent-url :foaf/account (account-data :account/url)]
                  [(account-data :account/url) :a :foaf/Account]
                  [(account-data :account/url) :foaf/accountServiceHomepage (account-data :account/platform)]
                  [(account-data :account/url) :foaf/accountName (account-data :account/name)]
                  ]]
   }])

(defn add-agent-account!-update [agent-url account-data]
  (cons
   {:prefixes my-prefixes
    :insert-data [agent-url :a :foaf/Agent]}
   (add-account!-update agent-url account-data)))

(stest/instrument)