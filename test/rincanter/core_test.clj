;; Original work
;; by Joel Boehland http://github.com/jolby/rincanter
;; January 24, 2010

;; Copyright (c) Joel Boehland, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

;; Modified work by svarcheg https://github.com/svarcheg/rincanter
;; May 5, 2015

(ns rincanter.core-test
  (:import (org.rosuda.REngine REXPInteger REXPDouble REXPString REXPLogical)
           (org.rosuda.REngine.Rserve RConnection))
  (:use (clojure test))
  (:use (incanter core stats))
  (:use (rincanter convert))
  (:use (rincanter core)))

;;taken from incanter information_theory_tests.clj
(defn =within [delta x y]
  (>= delta (abs (- x y))))

(defn jri-engine-fixture [test-fn]
  (get-r-connection)
  (test-fn)
  (.close ^RConnection  (get-r-connection)))

(deftest can-connect-to-R
  (is (not (= nil (get-r-connection)))))

(deftest to-r-conversions
  (is (= REXPLogical (class (to-r (into-array Byte/TYPE (map #'byte [1 2 3]))))))
  (is (= REXPInteger (class (to-r (into-array Integer/TYPE [1 2 3])))))
  (is (= REXPDouble (class (to-r (into-array Double/TYPE [1.0 2.0 3.0])))))
  (is (= REXPString (class (to-r (into-array String ["fee" "fie" "foe"])))))
  ;;test types with meta data hints set
  (is (= REXPLogical (class (to-r (with-meta [1 2 3] {:r-type REXPLogical})))))
  (is (= REXPInteger (class (to-r (with-meta [1 2 3] {:r-type REXPInteger})))))
  (is (= REXPDouble (class (to-r (with-meta [1 2 3] {:r-type REXPDouble})))))
  (is (= REXPDouble (class (to-r (with-meta [1.0 2.0 3.0] {:r-type REXPDouble})))))
  (is (= REXPString (class (to-r (with-meta ["fee" "fie" "foe"] {:r-type REXPString})))))
  ;;seq conversions
  (is (= REXPInteger (class (to-r [1 2 3]))))
  (is (= REXPDouble (class (to-r [1.9 2.0 3.9]))))
  (is (= (dataset ["c1" "c2"] '((1 2) (3 4))) (from-r (to-r (dataset ["c1" "c2"] '((1 2) (3 4))))))))

(deftest pass-through-int-vector
  (r-set! "iv1" (to-r [1 2 3]))
  (is (= [1 2 3] (r-get "iv1"))))

#_(deftest from-r-int-vector
  (r-eval "iv2 = c(1, 2, 3)")
  (is (= [1 2 3] (r-get "iv2"))))

(deftest pass-through-double-vector
  (r-set! "dv1" (to-r [1.0 2.0 3.0]))
  (is (= [1.0 2.0 3.0] (r-get "dv1"))))

(deftest from-r-double-vector
  (r-eval "dv2 = c(1.0, 2.0, 3.0)")
  (is (= [1.0 2.0 3.0] (r-get "dv2"))))

(deftest convert-dataframe-to-dataset
  (with-r-eval
    "data(iris)"
    ;;starts off an R dataframe, turns into an incanter dataset
    (is (= (type (r-get "iris")) incanter.core.Dataset))))

(deftest dataframe-dataset-dim-equivalence
  (is (= [150 5] (r-eval "dim(iris)")))
  (is (= [150 5] (dim (r-get "iris")))))

(deftest pass-through-dataframe-equivalence
  (with-r-eval
    "data(iris)"
    ;;convert iris dataframe to an incanter dataset, then convert back
    ;;to an R dataframe and set it in the R environment
    (r-set! "irisds" (to-r (r-get "iris")))
    ;;irisds is now an R dataframe it should be identical to iris dataframe
    (is (r-true (r-eval "identical(irisds, iris)")))))

(deftest dataframe-dataset-mean
  (with-data (r-get "iris")
    (is (=within 0.000001
                 (mean ($ :Sepal.Width))
                 ((r-eval "mean(iris$Sepal.Width)") 0)))))

(use-fixtures :once jri-engine-fixture)

