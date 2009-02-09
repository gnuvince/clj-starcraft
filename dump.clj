(set! *warn-on-reflection* true)

(ns starcraft.dump
  (:use [starcraft.replay.unpack])
  (:import [java.io File]))

(defn run []
  (doseq [f (map #(File. %) *command-line-args*)]
    (try
     (unpack f)
     (catch Exception e
       (println "Couldn't dump" f)))))

(time (run))
