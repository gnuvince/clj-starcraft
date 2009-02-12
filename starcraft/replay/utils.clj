(ns starcraft.replay.utils)

;; ;; TODO: This should probably be a lazy function, but let's wait
;; ;; until the lazy-seq code is merge into trunk.
;; (defn apm
;;   [actions]
;;   "Return a vector of the number of actions for each minute."
;;   (let [one-minute (* 24 60)]
;;     (loop [[action & actions] actions
;;            ns []
;;            n 0
;;            limit one-minute]
;;       (cond (nil? action) (conj ns n)
;;             (< (:tick action) limit) (recur actions ns (inc n) limit)
;;             :else (recur actions (conj ns n) 1 (+ limit one-minute))))))

(defn apm-aux
  [[a & as :as actions] n limit]
  (if (seq actions)
    (if (< (:tick a) (* limit 24 60))
      (apm-aux as (inc n) limit)
      (lazy-cons n (apm-aux as 1 (inc limit))))
    (lazy-cons n nil)))

(defn apm
  [actions]
  (apm-aux actions 0 1))
  

(defn avg
  [aseq]
  (/ (reduce + aseq) (count aseq)))

(defn apm-stats
  [actions]
  "Return the least, average and peek apm."
  (let [apms (apm actions)]
    [(apply min apms)
     (double (avg apms))
     (apply max apms)]))

(defn distribution
  [coll]
  (reduce (fn [m x] (assoc m x (inc (get m x 0)))) {} coll))

(defn unit-distribution
  [actions]
  "Return a map of the unit distribution for a set of actions."
  (distribution (for [action actions :when (or (= (:name action) "Train")
                                               (= (:name action) "Hatch"))]
                  (:unit-type action))))
