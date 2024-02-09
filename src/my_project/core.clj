(ns my-project.core
  (:use [my-project.colors])
  (:use [my-project.utils])
  (:use [my-project.keycaps])

  (:use [scad-clj.model])
  (:require [scad-clj.scad :refer [write-scad]])
  (:require [clojure.math :refer [PI to-radians]])

  (:refer-clojure :exclude [use include]))


;; Dimensions are taken from the svg
(defn place-screws
  "Places all `screw`s"
  [screw]

  (map (fn [pos] (translate pos screw))

       ;; Leftmost holes
       [[18.90 -25.77]
        [23.37 -63.97]

        [53.24 -22.37]
        [53.24 -60.47]

        [79.65 -22.37]
        [79.65 -60.47]

        ;; Rightmost holes
        [114.19 -25.77]
        [114.19 -63.97]]))


(def screw
  "Screw shape"
  (binding [*fn* 100]
    (cylinder 1.3 100)))


(def standoff
  "The stud around the screwhole"
  (binding [*fn* 100]
    (cylinder 2 100)))


(def standoffs
  "All the standoffs"
  (place-screws standoff))


(def screwholes
  "All the screwholes"
  (place-screws screw))


;; Outline of the keyboard chassis
;; Dimensions are taken from the svg
(def outline
  "A 2D shape of the outline of the keyboard."

  (polygon [;; Upper right corner
            [133.20 -7.52]
            [132.57 -6.87]

            ;; Slight bend
            [91.99 0.0]

            ;; Upper left corner
            [3.10  0.00]
            [2.04 -0.19]
            [0.41 -1.55]
            [0.00 -2.55]

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
            [130.09 -84.30]
            [131.16 -84.12]
            [132.78 -82.75]
            [133.20 -81.20]]))


(def trrs-cutout
  "Cutout for the TRRS connector connecting the two halves."

  (my-extrude 6
    (polygon
     [[-0.01 -2.40]
      [-0.01 -9.87]
      [18.85 -9.87]
      [18.85 -2.40]])))


;; TODO Measure some batteries and make a few variations
(def battery-cutout
  (my-extrude 8
    (intersection
      (offset -1 outline)
      (polygon
       [[150 -40]
        [ 82 -40]
        [ 82 0]
        [150 0]]))))

(def battery-cutout-2
  (my-extrude 8
    (intersection
      (offset -1 outline)
      (polygon
       [[50 -40]
        [21 -40]
        [21 0]
        [50 0]]))))


(def arduino-cutout
  "Cutout for the microcontroller."

  (my-extrude 100
    (polygon
     [[77 -35]
      [56 -35]
      [56 4.3]
      [77 4.3]])))


;; TODO Fix position
;; Position good enough for casing,
;; but not accurate for PCB
(def arduino
  (union
;;    (black
;;      (translate [65 0 0.1]
;;        (rotate [0 0 (/ PI 2)] (scad-clj.model/import "stl/arduino_header.stl"))))
    (blue
      (translate [66.5 0]
        (rotate [0 0 (/ PI 2)] (scad-clj.model/import "stl/arduino.stl"))))))
;;    (my-extrude 1.5
;;      (polygon
;;       [[73.955 -40.83]
;;        [55.395 -40.83]
;;        [55.395 0]
;;        [73.955 0]]))))


(def col-offsets
  "Y-axis offsets for each column."

  [19.725
   15.037
   12.664
   10.310
   12.624
   34.303
   36.611])


;; Why are these not the same??
(def key-spacing-x 19.05)
(def key-spacing-y 19.04)


(def switch
  "A single MX switch."

  (let [bottom-width 14
        bottom-height 6
        top-width 8
        top-height 5]
  (hull
   (translate [0 0 bottom-height] (my-cube [top-width top-width top-height]))
    (my-cube [bottom-width bottom-width bottom-height]))))


(defn switch-cutout
  "Cutout for switch."
  [& {:keys [size]

      :or {size :1u}}]
  (case size
    :1u (my-cube 14)
    :2uh (my-cube [14 33.5 14])))


(defn place-main-keys
  "Given a shape `key`, places all the main finger keys.
See `place-thumb-keys` for placement of the thumb keys.

If `key` is a function it is called with the keywords
`row`, `col`."
  [key]

  (translate [9.45 0]
  (for [nx (range 0 7)
        ny (range -2 1)
        :when (not (= [nx ny] [0 -2])) ;; Don't do key at [0,-2]
        :let [x (* nx key-spacing-x)
              y (- (* ny key-spacing-y)
                   (get col-offsets nx))]]

    (translate [x y]
      (if (fn? key)
        (apply key [:row (+ 2 ny) :col nx])
        key)))))


(defn place-thumb-keys
  "Given a shape `key`, places all the thumb keys.
See `place-main-keys` for placement of the finger keys.

If `key` is a function it is called."
  [inner-key outer-key]

  (let [inner-x 31.4475
        outer-x  3.4775
        inner-rot (/ PI 18)
        outer-rot (/ PI 6)]

    ;; Move both keys to correct row
    (translate [9.45 -72.05] ;; TODO Make variables

      ;; Inner thumb key
      (translate [inner-x 0]
                 (rotate [0 0 inner-rot]
                         (if (fn? inner-key) ;; TODO Make a function of this
                           (inner-key)
                           inner-key)))

      ;; Outer thumb key
      (translate [outer-x 0]
                 (rotate [0 0 outer-rot]
                         (if (fn? outer-key)
                           (outer-key)
                           outer-key))))))


(def all-keycaps
  "All the keycaps."

  (dark-grey
    (place-thumb-keys (partial keycap :row 13 :size :1.5u)
                      (partial keycap :row 41 :size :2uh))
    (place-main-keys keycap)))


(def switches
  "All the switches."

  (light-grey
  (union
    (place-thumb-keys switch
                      switch)
    (place-main-keys switch))))


(def switches-cutout
  "Cutouts for all the switches."

  (translate [0 0 -0.01]
             (union
              (place-thumb-keys switch-cutout
                                (partial switch-cutout :size :2uh))
              (place-main-keys switch-cutout))))


(def pcb-height 1.5)
(def pcb
  (grey
   (difference
    (my-extrude pcb-height outline)
    screwholes)))


(def bottom-tilt
  "How many degrees the bottom part of the case should be tilted."
  (to-radians 8))


(defn recess-cutout
  "The recessed area inside the case with studs around the screwholes"
  [height]

  (let [border-width 1]
    (difference
     (my-extrude height (offset (- border-width) outline))
     standoffs)))

(def rubber-feet-cutout
  (map (fn [pos]
         (translate pos
           (binding [*fn* 100]
             (cylinder 2.5 1.5)))) ;; TODO Measure diameter of feet

        ;; Upper right corner
       [[129 -11]

        ;; Upper left corner
        [5  -5.00]

        ;; Thumb upper corner
        [1 -62]

        ;; Thumb bottom corner
        [16 -88]

        ;; Bottom right corner
        [129 -81]]))


(def bottom-casing
  "The bottom part of the casing, with all cutouts."

  (let [height 15 ;; Number pulled out of arse
        recess 1.5]

    (dark-grey
     (difference

      ;; Rotate the whole bottom-casing
      (rotate [bottom-tilt 0 0]
        (difference
         (my-extrude height outline)

         (translate [0 0 (+ height 0.01)]

           ;; Flip recess and cutouts upside down (make height negative)
           (scale [1 1 -1]
             (recess-cutout recess)
             arduino-cutout
             battery-cutout
             battery-cutout-2
             trrs-cutout)
           screwholes)))

      rubber-feet-cutout

      ;; TODO Make a bounding box function to calculate this cube
      ;; Cut off the bottom to shape it into a wedge
      (translate [-20 -100 -50]
                 (my-cube [170 100 50] :center false))))))


;; TODO make bottom and top not be mirrored of each other
(def top-casing
  "The top part of the casing, with all cutouts."

  (let [height 5
        plate-thickness 1.5]

    (dark-grey
     (difference

      ;; The main body of the top-casing
      (my-extrude height outline)

      (translate [0 0 plate-thickness]
                 (recess-cutout height))

      ;; Cutouts
      switches-cutout
      screwholes))))


(def assembled
  "Fully assembled keyboard with all parts installed.
  Not suitable for print, should be used for previewing."

  (union
   bottom-casing
   (rotate [bottom-tilt 0 0]
     (translate [0 0 15.001] pcb)
     (translate [0 0 15] (scale [1 1 -1] arduino))
     (translate [0 0 (+ 20 pcb-height)] ;; TODO Make variable
                (scale [1 1 -1] top-casing)
                (translate [0 0 -1.5] ;; TODO Make variable
                           switches)
                (translate [0 0 6] ;; TODO make variable
                           all-keycaps)))))


(def exploded
  "All the printable parts laid out side by side."

  (union
   bottom-casing
   (translate [0 1 0] (scale [1 -1 1] top-casing))
   (translate [-10 0] (scale [-1 1 1] all-keycaps))))


(def out
  "The shape to render."
  (union
   assembled))


(def scad-libraries
  (list
    (scad-clj.model/use "scad-libraries/PseudoMakeMeKeyCapProfiles/MX_DES_Standard.scad")))



(defn -main
  "Converts the shape defined in `out` to openscad and saves it to `ergotravel.scad`"
  [& args]
  (spit "ergotravel.scad"
        (write-scad scad-libraries out)))

