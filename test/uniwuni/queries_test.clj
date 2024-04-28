(ns uniwuni.queries-test
  (:require [uniwuni [queries :as sq] [general :as sg] [general-test :as gt]]))

(require '[com.yetanalytics.flint.spec
           [prologue :as f.s.prologue]
           [axiom :as f.s.axiom]
           [query :as f.s.query]]
         '[clojure.test :refer [testing is deftest]])
(require '[clojure.test.check.generators :as gen])

(require '[clojure.spec.alpha :as s])
(require '[clojure.spec.test.alpha :as stest])

(deftest query-test
  (testing "Query generators"
    (gt/test-spec `sq/agent-of-channel?-query)
    (gt/test-spec `sq/is-embodied?-query)
    (gt/test-spec `sq/add-account!-update)
    (gt/test-spec `sq/add-agent-account!-update)))
