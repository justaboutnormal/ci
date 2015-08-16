(ns ci.util-test
  (:require [clojure.test :refer :all]
   [ci.util :refer :all]))

;Test setup
(def t-source-1 {:a {:m1 2 :m2 1}
                 :b {:m1 4 :m3 5}})

;Tests

(deftest test-reverse-ratings
  (testing "reverse-ratings"
    (is (= (reverse-ratings {:a {:m1 2}}) {:m1 {:a 2}}))
    (is (= (reverse-ratings t-source-1) {:m1 {:a 2 :b 4} :m2 {:a 1} :m3 {:b 5}}))))
