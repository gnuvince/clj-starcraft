(ns starcraft.dump
  (:use [starcraft.replay.unpack])
  (:import [java.io File]))

(defn run []
  (doseq [f (map #(File. %) *command-line-args*)]
    (try
     (println (count (unpack f)))
     (catch Exception e
       (println "Couldn't dump" f)
       (println e)
       (println (.printStackTrace e))))))

(time (run))
