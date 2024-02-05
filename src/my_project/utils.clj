(ns my-project.utils
  (:require [scad-clj.model :refer [extrude-linear translate cube]])
  (:refer-clojure :exclude [use include]))


(defn my-extrude
  "Extrude shape along Z axis, and place it at relative 0."
  [height & args]

  (translate [0 0 (/ height 2)]
             (apply extrude-linear {:height height} args)))


(defn my-cube
  "An improved `cube` function. Can take either a single number as
  dimensions, in which case `x` `y` and `z` will be the same,
  otherwise it takes a vector of `[x y z]`."
  [dimensions & {:keys [center]
                 :or {center true}}]

  (let [[x y z] (if (vector? dimensions)
                  dimensions
                  [dimensions dimensions dimensions])]
    (translate [0 0 (if center (/ z 2) 0)]
               (cube x y z :center center))))
