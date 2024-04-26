(ns uniwuni.queries-test
  (:require [uniwuni.queries :as sut]))
(require '[com.yetanalytics.flint.spec
           [prologue :as f.s.prologue]
           [axiom :as f.s.axiom]
           [query :as f.s.query]]
           '[clojure.test :refer :all])
(require '[clojure.test.check.generators :as gen])

(require '[clojure.spec.alpha :as s])
(require '[clojure.spec.test.alpha :as stest])


(def iri-gen
    (gen/one-of (list (s/gen uri?)
                 (gen/let [x gen/string-alphanumeric] (str "unia:" "word" x "word")))))

(def overwrites {`f.s.axiom/iri-or-prefixed-spec (constantly iri-gen)})

(defn test-spec [f]  (is (nil? (:failure (stest/check f {:gen overwrites})))))

(deftest agent-of-channel?-query-test
  (testing "Channel agent query "
    (test-spec `sut/agent-of-channel?-query)
    (test-spec `sut/is-embodied?-query)))
