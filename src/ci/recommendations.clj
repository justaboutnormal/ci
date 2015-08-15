(ns ci.recommendations
  (:require [clojure.set :as c-set]
   [clojure.math.numeric-tower :as math]))


(defn sum-ratings [person keys]
  (reduce (fn [s v] (+ s (person v))) 0 keys))

(defn sum-squares [person keys]
  (reduce (fn [s v] (+ s (math/expt (person v) 2))) 0 keys))

(defn sum-product [p1 p2 keys]
  (reduce (fn [s v] (+ s (* (p1 v) (p2 v)))) 0 keys))

(defn pearson-cc
  "Gets the Pearson Product-moment Corelation Coeficient between p1 and p2"
  ([p1 p2] (pearson-cc p1 p2 (filter p1 (keys p2))))
  ([p1 p2 shared]
   (if (= 0 (count shared))
     0
     (let [sum-p1 (sum-ratings p1 shared)
           sum-p2 (sum-ratings p2 shared)
           sum-sq-p1 (sum-squares p1 shared)
           sum-sq-p2 (sum-squares p2 shared)
           sum-prod (sum-product p1 p2 shared)]
       (/ (- sum-prod (/ (* sum-p1 sum-p2) (count shared)))
         (math/sqrt (*
                      (- sum-sq-p1 (/ (math/expt sum-p1 2) (count shared)))
                      (- sum-sq-p2 (/ (math/expt sum-p2 2) (count shared))))))))))

(defn sim-scores [source p1-key sim-func count]
  (take count
    (->> (map (fn [[k p2]] [k (sim-func (source p1-key) p2)])
           (filter (fn [[k _]] (not= k p1-key)) source))
      (sort-by last)
      (reverse))))

(defn need-review [source person]
  (-> (set (flatten (map (fn [[k v]] (keys v)) source)))
    (c-set/difference (set (map (fn [[k _]] k) person)))))

(defn unreviewed-scores
  "Takes a map of reviewers with their reviews and a list of products to review and returns the list of reviewers without unneccasary reviews."
  [reviewers to-review]
  (->> (map (fn [[r scores]] [r (into {} (filter (fn [[prod _]] (contains? to-review prod)) scores))]) reviewers)
    (filter (fn [[k v]] (not (empty? v))))
    (into {})))

(defn pos-correlations [sim-scores]
  (filter (fn [[k v]] (< 0 v)) sim-scores))

(defn corrected-prod [person multiplyer]
  (into {} (map (fn [[k v]] [k (* v multiplyer)]) person)))

(defn weighted-scores-by-reviewer [source sim-vals]
  (->> (map (fn [[p2 score]]
              [p2 (corrected-prod (source p2) score)])
         (pos-correlations sim-vals))
    (filter (fn [[k v]] (not (empty? v))))
    (into {})))

(defn adjust-reviews
  "given map of reviewers, similarity scores for reviewers, and list to review returns adjusted reviews for each reviewer"
  [source sim-vals to-review]
  (->> (weighted-scores-by-reviewer (unreviewed-scores source to-review) sim-vals)
    (map (fn [[_ reviews]] reviews))))

(defn freq-reviews
  "takes a list of adjusted reviews and returns a map ['product' 'frequency-each-product-was-reviewed']"
  [reviews]
  (frequencies (mapcat (fn [m] (map (fn [[k v]] k) m)) reviews)))

(defn recommend
  "Returns vector of vectors with shape ['product' 'predicted-rating-for-person']"
  ([source person-key] (recommend source person-key 100 pearson-cc))
  ([source person-key result-count sim-func]
   (let [adj-reviews (adjust-reviews source (sim-scores source person-key sim-func result-count) (need-review source (source person-key)))
         sum-reviews (apply merge-with + adj-reviews)
         freq (freq-reviews adj-reviews)]
     (map (fn [[k v]] [k (/ v (freq k))]) sum-reviews))))

(defn reverse-ratings
  "Takes a map of maps and reverses it such that {:a {:1 'a1' :2 'a2'} :b {:1 'b1' :3 'b3'}}
  returns {:1 {:a 'a1' :b 'b1'} :2 {:a 'a2'} :3 {:b 'b3'}} "
  [source] (apply merge-with merge
                             (for [[ok ov] source
                                   [ik iv] ov]
                               {ik {ok iv}})))
