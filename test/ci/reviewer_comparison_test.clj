(ns ci.reviewer-comparison-test
  (:require [clojure.test :refer :all]
   [ci.reviewer-comparison :refer :all]
   [ci.util :refer :all]))

;Test setup
(defn mov [m0 m1 m2 m3 m4 m5 m6]
  (into {}
    (filter (fn [[k v]] (not= v nil))
      {:m0 m0 :m1 m1 :m2 m2 :m3 m3 :m4 m4 :m5 m5 :m6 m6})))

(def t-source-1 {:a (mov 2 1 4 4 2 3 1)
                 :b (mov 2 4 3 3 3 1 5)
                 :c (mov 3 2 5 5 3 nil nil)
                 :d (mov 3 2 5 5 3 nil nil)
                 :e (mov 2 1 4 4 3 3 2)})

(def t-source-2 (into {} (filter (fn n-eq [[k v]] (not= :d k)) t-source-1)))

;Tests

(deftest test-pearson-score
  (testing "pearson-cc via sim-scores"
    ; results-1 verify scores between exact, positively correlated, and negatively correlated
    ; results-2 verify order of results
    ; results-3 test reverse (if you like :m1 you may like :m6...)
    (let [results-1 (sim-scores t-source-1 :c pearson-cc 100)
          results-2 (sim-scores t-source-2 :c pearson-cc 100)
          results-3 (sim-scores (reverse-ratings t-source-1) :m1 pearson-cc 100)]
      (are [x y] (= x y)
                 (count results-1) 4
                 (set results-1) (set [[:d 1] [:a 1] [:e 0.9432422182837985] [:b -0.26352313834736496]])
                 results-2 [[:a 1] [:e 0.9432422182837985] [:b -0.26352313834736496]]
                 results-3 [[:m6 0.9707253433941511] [:m4 0.45643546458763845] [:m0 0.0] [:m3 -0.48795003647426655] [:m2 -0.48795003647426655] [:m5 -1]]))))

(deftest test-unreviewed
  (testing "unreviewed"
    (is (= (need-review t-source-1 (t-source-1 :c)) #{:m5 :m6}))))

(deftest test-unreviewed-scores
  (testing "unreviewed-scores"
    (is (= (unreviewed-scores t-source-2 (need-review t-source-2 (t-source-2 :c))) {:a {:m5 3 :m6 1} :e {:m5 3 :m6 2} :b {:m5 1 :m6 5}}))))

(deftest test-adjust-reviews
  (testing "adjust-reviews"
    (is (= (adjust-reviews t-source-1 (sim-scores t-source-1 :c pearson-cc 100) (need-review t-source-1 (t-source-1 :c)))
          '({:m5 3N, :m6 1N} {:m5 2.8297266548513953, :m6 1.886484436567597})))))

(deftest test-sim
  (testing "recommend"
    (is (= (recommend t-source-1 :c)
          [[:m5 2.9148633274256976] [:m6 1.4432422182837985]]))))
