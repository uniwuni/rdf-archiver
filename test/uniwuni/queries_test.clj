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


(def overwrites {})

(defn test-spec [f] (is (:result (:clojure.spec.test.check/ret (first (stest/check f {:gen overwrites}))))))

(deftest query-test
  (testing "Query generators"
    (test-spec `sut/agent-of-channel?-query)
    (test-spec `sut/is-embodied?-query)
    (test-spec `sut/add-account!-update)
    (test-spec `sut/add-agent-account!-update)
    ))
