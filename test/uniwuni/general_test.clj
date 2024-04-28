(ns uniwuni.general-test
  (:require [uniwuni.general :as sg]
            [clojure.test :as t]))
(require '[com.yetanalytics.flint.spec
           [prologue :as f.s.prologue]
           [axiom :as f.s.axiom]
           [query :as f.s.query]]
           '[clojure.test :refer [testing is deftest]])
(require '[clojure.test.check.generators :as gen])

(require '[clojure.spec.alpha :as s])
(require '[clojure.spec.test.alpha :as stest])


(def overwrites {})

(defn test-spec [f] (is (:result (:clojure.spec.test.check/ret (first (stest/check f {:gen overwrites}))))))

(deftest uri-test
  (testing "URI function"
    (test-spec `sg/uri)))
