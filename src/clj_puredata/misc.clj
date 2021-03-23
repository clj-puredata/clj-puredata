(ns clj-puredata.misc
  (:import [java.io File]
           [javax.imageio ImageIO]))

(defn color-runtime
  [r g b]
  (int (dec (+ (* r -65536)
               (* g -256)
               (* b -1)))))

(defn color-file
  ([r g b]
   (let [[r_ g_ b_] (map #(-> % (/ 4) Math/floor) [r g b])]
     (int (dec (+ (* r_ -4096)
                  (* g_ -64)
                  (* b_ -1))))))
  ([[r g b]]
   (color-file r g b)))

(defn- hue2rgb
  "Support function for `hsl2rgb`."
  [p q t]
  (let [t (cond (< t 0) (inc t)
                (> t 1) (dec t)
                :else t)]
    (cond (< t 1/6) (+ p (* (- q p) 6 t))
          (< t 1/2) q
          (< t 2/3) (+ p (* (- q p) 6 (- 2/3 t)))
          :else p)))

(defn hsl2rgb
  "Create [r g b] (red, green, blue) Vector from Hue, Saturation, Light parameters.
  Assumes all parameters are in [0 .. 1] range.
  Return rgb in [0 .. 255] range.
  Credit: https://stackoverflow.com/a/9493060"
  [h s l]
  (->> (if (= s 0)
         [l l l]
         (let [q (if (< l 0.5)
                   (* l (inc s))
                   (+ l (- s (* l s))))
               p (- (* 2 l) q)]
           [(hue2rgb p q (+ h 1/3))
            (hue2rgb p q h)
            (hue2rgb p q (- h 1/3))]))
       (map #(min (int (* 256 %)) 255))))

(defn import-image
  [image-filename pixel-size]
  (let [f (ImageIO/read (File. image-filename))
        r (.getData f)
        w (.getWidth r)
        h (.getHeight r)
        out (int-array 4) ;; assume png pixel is 4 byte (rgba)
        size pixel-size
        canvasses (for [y (range h)
                        x (range w)
                        :let [c (.getPixels r x y 1 1 nil)]
                        :when (> (nth c 3) 0)] ;; ignore pixels with alpha == 0
                    [:cnv {:x (+ (* x size) (/ size 2))
                           :y (+ (* y size) (/ size 2))
                           :width (dec size)
                           :height (dec size)
                           :bg-color (apply color-file (take 3 c))}])]
    canvasses))
