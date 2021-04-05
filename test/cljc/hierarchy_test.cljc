(ns hierarchy-test
  (:require  #?(:clj [clojure.test :refer [deftest testing is]]
                :cljs [cljs.test :refer [deftest testing is] :include-macros true])
             [knowtator.hierarchy :as sut]
             [clojure.zip :as zip]))

(def test-hierarchy (-> (make-hierarchy)
                      (derive :B :A)
                      (derive :C :A)
                      (derive :D :B)
                      (derive :E :B)
                      (derive :F :C)))
(deftest roots-test
  (is (= [:A]
        (sut/roots test-hierarchy))))

(deftest hierarchy-zipper
  (is (= [:A nil]
        (first (sut/hierarchy-zippers test-hierarchy))))

  (is (= [:A :B :D :E :C :F]
        (->> test-hierarchy
          sut/hierarchy-zippers
          first
          (tree-seq zip/branch? sut/child-nodes)
          (map zip/node)))))
