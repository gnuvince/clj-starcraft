(ns starcraft.replay.utils
  (:use [clojure.contrib.seq-utils]))

(def *one-minute* (* 24 60))

(defn avg
  "Return the average of a sequence."
  [aseq]
  (/ (reduce + aseq) (count aseq)))

(defn- apm*
  [[a & as :as actions] n limit]
  (lazy-seq
   (if-let [actions (seq actions)]
     (if (< (:tick a) (* limit *one-minute*))
       (apm* as (inc n) limit)
       (cons n (apm* as 1 (inc limit))))
     [n])))

(defn apm
  "Return a lazy seq of the number of actions for each minute."
  [actions]
  (apm* actions 0 1))

(defn apm-stats
  "Return the least, average and peak apm."
  [actions]
  (let [apms (apm actions)]
    [(apply min apms)
     (int (avg apms))
     (apply max apms)]))

(defn unit-distribution
  "Return a map of the unit distribution for a set of actions."
  [actions]
  ;; Train is the standard unit creation action for Terran
  ;; and Protoss, Hatch is the standard unit creation action
  ;; for Zerg. Archons have their own action types.
  (let [m {"Train" #(:unit-type %)
           "Hatch" #(:unit-type %)
           "Merge Archon" (constantly "Archon")
           "Merge Dark Archon" (constantly "Dark Archon")}]
    (frequencies
     (for [{action-name :name :as action} actions
           :when (m action-name)]
       ((m action-name) action)))))

(defn action-distribution
  "Returns a map of the action distribution for a set of actions."
  [actions]
  (frequencies (map :name actions)))
