(ns starcraft.replay.utils)

;; TODO: This should probably be a lazy function, but let's wait
;; until the lazy-seq code is merge into trunk.
(defn apm [actions]
  "Return a vector of the number of actions for each minute."
  (let [one-minute (* 24 60)]
    (loop [[action & actions] actions
           ns []
           n 0
           limit one-minute]
      (cond (nil? action) ns
            (< (:tick action) limit) (recur actions ns (inc n) limit)
            :else (recur actions (conj ns n) 1 (+ limit one-minute))))))


(defn avg [coll]
  (let [[total length] (reduce (fn [[t l] n]
                                 [(+ t n) (inc l)])
                               [0 0] coll)]
    (/ total length)))

(defn apm-stats [actions]
  "Return the least, average and peek apm."
  (let [apms (apm actions)]
    [(apply min apms)
     (double (avg apms))
     (apply max apms)]))

(defn distribution [coll]
  (reduce (fn [m x] (assoc m x (inc (get m x 0)))) {} coll))

(defn unit-distribution [actions]
  (distribution (for [action actions :when (or (= (:name action) "Train")
                                               (= (:name action) "Hatch"))]
                  (:unit-type action))))
