(ns starcraft.replay.utils
  (:use [clojure.contrib.seq-utils]))
  
(defn avg
  "Return the average of a sequence."
  [aseq]
  (/ (reduce + aseq) (count aseq)))

(defn- apm-aux
  [[a & as :as actions] n limit]
  (if (seq actions)
    (if (< (:tick a) (* limit 24 60))
      (apm-aux as (inc n) limit)
      (lazy-cons n (apm-aux as 1 (inc limit))))
    [n]))

(defn apm
  "Return a lazy seq of the number of actions for each minute."
  [actions]
  (apm-aux actions 0 1))

(defn apm-stats
  "Return the least, average and peek apm."
  [actions]
  (let [apms (apm actions)]
    [(apply min apms)
     (int (avg apms))
     (apply max apms)]))

(defn unit-distribution
  "Return a map of the unit distribution for a set of actions."
  [actions]
  (let [m {"Train" #(:unit-type %)
           "Hatch" #(:unit-type %)
           "Merge Archon" (constantly "Archon")
           "Merge Dark Archon" (constantly "Dark Archon")}]
    (frequencies
     (for [{action-name :name :as action} actions
           :when (m action-name)]
       ((m action-name) action)))))
