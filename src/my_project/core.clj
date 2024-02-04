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
    (cylinder 1.3 100)))

(def standoff
  (binding [*fn* 100]
    (cylinder 2 100)))

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
  (hull
    (translate [0 0 6] (cube 8 8 5))
    (cube 14 14 5)))

(defn switch-cutout
  "Cutout for switch."
  [& {:keys [size]
      :or {size :1u}}]
  (case size
    :1u (cube 14 14 14)
    :2uh (cube 14 33.5 14)))

(defn keycap-angle
  "Return the angle for the top face of keycap based on row"
  [row]
  (nth [-10 -3 6] row))

(defn keycap
  "Generate a keycap of the given size and row."
  [& {:keys [row
             size
             angle
             bottom-width
             bottom-height
             top-width
             top-height]
      :or {row 1
           size :1u
           angle (keycap-angle row)
           bottom-width (case size
                          :1u 16
                          :1.5u 24
                          :2u 35
                          :2uh 16)
           bottom-height (case size
                           :1u 16
                           :1.5u 16
                           :2u 16
                           :2uh 35)
           top-width (case size
                       :1u 12
                       :1.5u 20
                       :2u 35
                       :2uh 12)
           top-height (case size
                        :1u 12
                        :1.5u 12
                        :2u 12
                        :2uh 30)}}]
  (hull
   (translate [0 1/2 6]
              (rotate [(to-radians angle) 0 0]
                      (cube top-width top-height 1)))
   (cube bottom-width bottom-height 1)))


(defn place-main-keys [key]
  (translate [9.45 0]
  (for [nx (range 0 7)
        ny (range -2 1)
        :when (not (= [nx ny] [0 -2])) ;; Don't do key at [0,-2]
        :let [x (* nx key-spacing-x)
              y (- (* ny key-spacing-y)
                   (get col-offsets nx))]]

    (translate [x y 0]
      (if (fn? key)
        (apply key [:row (+ 2 ny) :col nx])
        key)))))


(defn place-thumb-keys [inner-key outer-key]
  ;; Move both keys to correct row
  (translate [9.45 -72.05] 
   ;; Inner thumb key
   (translate [31.4475 0]
     (rotate [0 0 (to-radians 10)]
       (if (fn? inner-key)
         (apply inner-key [:row 2])
         inner-key)))
   ;; Outer thumb key
   (translate [3.4775 0 0]
     (rotate [0 0 (to-radians 30)]
       (if (fn? outer-key)
         (apply outer-key [:row 2])
         outer-key)))))

(def keycaps
  (union
    (place-thumb-keys (partial keycap :size :1.5u)
                      (partial keycap :size :2uh))
    (place-main-keys keycap)))
   
(def switches
  (union
    (place-thumb-keys switch
                      switch)
    (place-main-keys switch)))

(def switches-cutout
  (union
    (place-thumb-keys switch-cutout
                      switch-cutout)
    (place-main-keys switch-cutout)))

;; The bottom part of the casing, with all cutouts
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

;; The top part of the casing, with all cutouts
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
   switches-cutout
   screwholes))

(def assembled
  (union
   bottom-casing
   (translate [0 0 5]
              (scale [1 1 -1] top-casing))
   (light-grey
    (translate [0 0 8]
               switches))
   (translate [0 0 15]
              keycaps)))

(def out
  (union
   assembled))

(defn -main
  [& args]
  (spit "ergotravel.scad"
        (write-scad out)))
