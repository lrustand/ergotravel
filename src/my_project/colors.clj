(ns my-project.colors
  (:require [scad-clj.model :refer [color]])
  (:refer-clojure :exclude [use include]))


(def red (partial color [1 0 0]))
(def green (partial color [0 1 0]))
;;(def blue (partial color [0.19 0.525 0.627]))
(def blue (partial color [40/255 90/255 95/255]))
(def purple (partial color [1 0 1]))
(def black (partial color [0 0 0]))
(def white (partial color [1 1 1]))
(def grey (partial color [0.5 0.5 0.5]))
(def dark-grey (partial color [0.25 0.25 0.25]))
(def light-grey (partial color [0.75 0.75 0.75 0.4]))
