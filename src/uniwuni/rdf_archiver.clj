(ns uniwuni.rdf-archiver
  (:gen-class))

(require   '[uniwuni.general :as general]
           '[uniwuni.config :as config]
           '[uniwuni.queries :as queries]
           '[uniwuni.videohandler :as videohandler])


(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))
