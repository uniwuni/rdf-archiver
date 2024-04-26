(ns uniwuni.queries-test
  (:require [uniwuni [queries :as sq] [general :as sg]]))
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
    (test-spec `sq/agent-of-channel?-query)
    (test-spec `sq/is-embodied?-query)
    (test-spec `sq/add-account!-update)
    (test-spec `sq/add-agent-account!-update)
    ))

; TODO MOVE THIS

(deftest uri-test
  (testing "URI function"
    (test-spec `sg/uri)))
