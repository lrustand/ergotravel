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


(defn keycap
  "Generate a keycap of the given `size` and `row`."
  [& {:keys [row
             col
             size
             height]
      :or {row 1
           col 0
           size :1u
           height 6}}]

;;module keycap(keyID = 0, cutLen = 0, visualizeDish = false, rossSection = false, Dish = true, Stem = false, crossSection = true,Legends = false, homeDot = false, Stab = 0) {
(scad-clj.model/call "keycap" row 0 false false true true false false false false 0))
