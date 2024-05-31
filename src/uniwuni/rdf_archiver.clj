(ns uniwuni.rdf-archiver
  (:gen-class) (:require [uniwuni.general :as general]
                         [uniwuni.config :as config]
                         [uniwuni.queries :as queries]
                         [uniwuni.videohandler :as videohandler]
                         [uniwuni.migration :as migration]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if (= (first args) "migrate")
    (migration/add-all-videos)))
