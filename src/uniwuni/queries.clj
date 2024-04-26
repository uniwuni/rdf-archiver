(ns uniwuni.queries)
(require '[com.yetanalytics.flint :as f])

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

(defn agent-of-channel?-query [account-url]
  {:prefixes my-prefixes
    :select ['?agent]
    :where [{'?agent {:a #{:foaf/Agent}
                      :foaf/account #{account-url}}}]
    })

(defn is-embodied?-query [manifestation-url]
  {:prefixes my-prefixes
   :ask []
   :where [{'?video {:a #{:frbr/Expression}
                     :frbr/embodiment #{manifestation-url}}}]})


(defn add-account!-update [agent-url account-data]
  [{:prefixes my-prefixes
   :insert-data [[[agent-url :foaf/name (account-data :name)]
                  [agent-url :foaf/account (account-data :url)]
                  [(account-data :url) :a :foaf/Account]
                  [(account-data :url) :foaf/accountServiceHomepage (account-data :platform)]
                  [(account-data :url) :foaf/accountName (account-data :name)]
                  ]]
   }])

(defn add-agent-account!-update [agent-url account-data]
  (cons
   {:prefixes my-prefixes
    :insert-data [agent-url :a :foaf/Agent]}
   (add-account!-update agent-url account-data)))
