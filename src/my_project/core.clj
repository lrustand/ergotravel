(ns my-project.core
  (:use [scad-clj.scad])
  (:use [scad-clj.model])
  (:use [clojure.math]))


(def red (partial color [1 0 0]))
(def green (partial color [0 1 0]))
(def blue (partial color [0 0 1]))
(def purple (partial color [1 0 1]))
(def black (partial color [0 0 0]))
(def white (partial color [1 1 1]))
(def grey (partial color [0.5 0.5 0.5]))
(def dark-grey (partial color [0.25 0.25 0.25]))
(def light-grey (partial color [0.75 0.75 0.75 0.4]))

;; Dimensions are taken from the svg
(defn place-screws [screw]
  (map (fn [pos]
         (translate pos screw))

       ;; Leftmost holes
       [[18.9 -25.77]
        [23.37 -63.97]

        [53.24 -22.37]
        [53.24 -60.47]

        [79.65 -22.37]
        [79.65 -60.47]

        ;; Rightmost holes
        [114.19 -25.77]
        [114.19 -63.97]]))

(def screw
  (binding [*fn* 100]
    (cylinder 1.3 10)))

(def standoff
  (binding [*fn* 100]
    (cylinder 2 10)))

(def standoffs
  (place-screws standoff))

(def screwholes
  (place-screws screw))

;; Outline of the keyboard chassis
;; Dimensions are taken from the svg
(def outline
  (polygon [;; Upper right corner
            [133.2 -7.52]
            [132.57 -6.87]
            
            ;; Slight bend
            [91.99 0.0]

            ;; Upper left corner
            [3.1 0.0]
            [2.04 -0.19]
            [0.41 -1.55]
            [0.0 -2.55]

            ;; Thumb to left edge
            [0.0 -56.44]

            ;; Thumb top corner
            [-3.89 -58.93]
            [-4.56 -60.49]

            ;; Thumb bottom corner
            [14.03 -92.26]
            [15.44 -92.64]

            ;; Thumb to bottom edge
            [29.73 -84.31]

            ;; Bottom right corner
            [130.09 -84.3]
            [131.16 -84.12]
            [132.78 -82.75]
            [133.2 -81.2]]))

(def trrs-cutout
  (extrude-linear {:height 10}
    (polygon
     [[0 -1.33]
      [0 -9.85]
      [18.8 -9.85]
      [18.8 -1.33]])))

;; Might not be needed
(def usb-cutout)
(def battery-cutout)

;; TODO fix X pos + width
(def arduino-cutout
  (extrude-linear {:height 10}
    (polygon
     [[73.955 -40.83]
      [55.395 -40.83]
      [55.395 4.3]
      [73.955 4.3]])))

(def col-offsets
  [19.725
   15.037
   12.664
   10.31
   12.624
   34.303
   36.611])

;; Why are these not the same??
(def key-spacing-x 19.05)
(def key-spacing-y 19.04)

(def switch
  (cube 14 14 14))

(def switch-2u
  (cube 14 33.5 14))

(defn place-main-keys [key]
  (translate [9.45 0]
  (for [nx (range 0 7)
        ny (range -2 1)
        :when (not (= [nx ny] [0 -2])) ;; Don't do key at [0,-2]
        :let [x (* nx key-spacing-x)
              y (- (* ny key-spacing-y)
                   (get col-offsets nx))]]

    (translate [x y 0] key))))

(defn place-thumb-keys [inner-key outer-key]
  ;; Move both keys to correct row
  (translate [9.45 -72.05] 
   ;; Inner thumb key
   (translate [31.4475 0]
     (rotate [0 0 (to-radians 10)]
       inner-key))
   ;; Outer thumb key
   (translate [3.4775 0 0]
     (rotate [0 0 (to-radians 30)]
       outer-key))))

(def main-switches
  (place-main-keys switch))

(def thumb-switches
  (place-thumb-keys switch
                    switch-2u))

(def switches
  (union
   thumb-switches
   main-switches))

(def bottom-casing
  (difference
   (extrude-linear {:height 5}
     outline)
   (difference
    (translate [1.35 -1.7 1.5]
     (scale [0.98 0.96 1]
            (extrude-linear {:height 4}
              outline)))
    standoffs)
   arduino-cutout
   trrs-cutout
   screwholes))

(def top-casing
  (difference
   (extrude-linear {:height 5}
     outline)
   (difference
    (translate [1.35 -1.7 1.5]
     (scale [0.98 0.96 1]
            (extrude-linear {:height 4}
              outline)))
    standoffs)
   switches
   screwholes))

(def assembled
  (union
   bottom-casing
   (translate [0 0 5]
     (scale [1 1 -1] top-casing))))

(def out
  (union
   assembled))

(defn -main
  [& args]
  (spit "ergotravel.scad"
        (write-scad out)))
