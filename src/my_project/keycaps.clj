(ns my-project.keycaps
  (:use [my-project.utils])

  (:require [scad-clj.model]))


(defn keycap-angle
  "Return the angle for the top face of keycap based on `row`"
  [row]
  (nth [-10 -3 6] row))


(defn top-width
  "Return `top-width` for given `size`."
  [size]
  (case size
    :1u 12
    :1.5u 20
    :2u 35
    :2uh 12))


(defn bottom-width
  "Return `bottom-width` for given `size`."
  [size]
  (case size
    :1u 16
    :1.5u 24
    :2u 35
    :2uh 16))


(defn bottom-height
  "Return `bottom-height` for given `size`."
  [size]
  (case size
    :1u 16
    :1.5u 16
    :2u 16
    :2uh 35))


(defn top-height
  "Return `top-height` for given `size`."
  [size]
  (case size
    :1u 12
    :1.5u 12
    :2u 12
    :2uh 30))


;; TODO Make actual keycap profile
(defn keycap
  "Generate a keycap of the given `size` and `row`."
  [& {:keys [row
             col
             size
             angle
             height]
      :or {row 1
           col 0
           size :1u
           angle (keycap-angle row)
           height 6}}]

  (let [bottom-width (bottom-width size)
        bottom-height (bottom-height size)
        top-width (top-width size)
        top-height (top-height size)]

    (hull
     (translate [0 1/2 height] ;; TODO Make variable
                (rotate [(to-radians angle) 0 0] ;; Rotate top face
                        (my-cube [top-width top-height 1])))
     (my-cube [bottom-width bottom-height 1]))))
