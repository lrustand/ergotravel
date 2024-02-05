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
            [3.10  0.0]
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
            [130.09 -84.3]
            [131.16 -84.12]
            [132.78 -82.75]
            [133.20 -81.2]]))


;; TODO Fix dimensions
(def trrs-cutout
  "Cutout for the TRRS connector connecting the two halves."

  (my-extrude 5
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
  "Cutout for the microcontroller."

  (my-extrude 100
    (polygon
     [[73.955 -40.83]
      [55.395 -40.83]
      [55.395 4.3]
      [73.955 4.3]])))


(def col-offsets
  "Y-axis offsets for each column."
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
             size
             angle
             height
             bottom-width
             bottom-height
             top-width
             top-height]
      :or {row 1
           size :1u
           angle (keycap-angle row)
           height 6
           bottom-width (bottom-width size)
           bottom-height (bottom-height size)
           top-width (top-width size)
           top-height (top-height size)}}]
  (hull
   (translate [0 1/2 height] ;; TODO Make variable
              (rotate [(to-radians angle) 0 0] ;; Rotate top face
                      (my-cube [top-width top-height 1])))
   (my-cube [bottom-width bottom-height 1])))


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

If `key` is a function it is called with the keyword `row`=2."
  [inner-key outer-key]

  ;; Move both keys to correct row
  (translate [9.45 -72.05] ;; TODO Make variables
   ;; Inner thumb key
   (translate [31.4475 0] ;; TODO Make variable
     (rotate [0 0 (to-radians 10)]
       (if (fn? inner-key)
         (apply inner-key [:row 2])
         inner-key)))
   ;; Outer thumb key
   (translate [3.4775 0] ;; TODO Make variable
     (rotate [0 0 (to-radians 30)]
       (if (fn? outer-key)
         (apply outer-key [:row 2])
         outer-key)))))


(def keycaps
  "All the keycaps."
  (union
    (place-thumb-keys (partial keycap :size :1.5u)
                      (partial keycap :size :2uh))
    (place-main-keys keycap)))


(def switches
  "All the switches."
  (union
    (place-thumb-keys switch
                      switch)
    (place-main-keys switch)))


(def switches-cutout
  "Cutouts for all the switches."

  (translate [0 0 -0.01]
             (union
              (place-thumb-keys switch-cutout
                                (partial switch-cutout :size :2uh))
              (place-main-keys switch-cutout))))


(def pcb-height 1.5)
(def pcb
  (my-extrude pcb-height outline))


(def bottom-tilt
  "How many degrees the bottom part of the case should be tilted."
  (to-radians 8))


(def bottom-casing
  "The bottom part of the casing, with all cutouts."

  (let [height 15 ;; Number pulled out of arse
        recess 1.5]

    (difference
      ;; Rotate the whole bottom-casing
      (rotate [bottom-tilt 0 0]
        (difference
         (my-extrude height outline)

         ;; The recessed area inside the case
         ;; with studs around the screwholes
         (translate [0 0 (+ height 0.01)]

           ;; Flip recess and cutouts upside down (make height negative)
           (scale [1 1 -1]
             (difference
               (my-extrude recess (offset -1 outline))
               standoffs)
             arduino-cutout
             trrs-cutout)
           screwholes)))

      ;; TODO Make a bounding box function to calculate this cube
      ;; Cut off the bottom to shape it into a wedge
      (translate [-20 -100 -50]
                 (my-cube [170 100 50] :center false)))))


;; TODO make bottom and top not be mirrored of each other
(def top-casing
  "The top part of the casing, with all cutouts."

  (let [height 5
        plate-thickness 1.5]

    (difference
     ;; The main body of the top-casing
     (my-extrude height outline)

     ;; The recessed area inside the case
     ;; with studs around the screwholes
     (translate [0 0 plate-thickness]
       (difference
         (my-extrude height (offset -1 outline))
         standoffs))

     ;; Cutouts
     switches-cutout
     screwholes)))


(def assembled
  "Fully assembled keyboard with all parts installed.
  Not suitable for print, should be used for previewing."

  (union
   (dark-grey bottom-casing)
   (rotate [bottom-tilt 0 0]
     (translate [0 0 20] ;; TODO Make variable
                (scale [1 1 -1] (dark-grey top-casing))
                (light-grey
                 (translate [0 0 -1.5] ;; TODO Make variable
                            switches))
                (dark-grey
                 (translate [0 0 8] ;; TODO make variable
                            keycaps))))))


(def out
  "The shape to render."
  (union
   assembled))


(defn -main
  "Converts the shape defined in `out` to openscad and saves it to `ergotravel.scad`"
  [& args]
  (spit "ergotravel.scad"
        (write-scad out)))
