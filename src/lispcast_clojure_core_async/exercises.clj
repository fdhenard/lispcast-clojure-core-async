(ns lispcast-clojure-core-async.exercises
  (:require [clojure.string :as string]
            [clojure.core.async :as async
               :refer [go chan <! >!]]
            [lispcast-clojure-core-async.factory :as f]
            [lispcast-clojure-core-async.core :as core]))

(comment


  (time (f/attach-wheel :body :wheel))
  (time (f/box-up {}))
  (time (f/put-in-truck {}))

  (time (core/build-car))


  ;; 2.2

  (go
    (time
     (dotimes [x 10]
       (println "x = " x))))

  (core/build-car 1)


  )

;; ex 2.3
(defn start-ten! []
  (dotimes [x 10]
    (go
      (time
       (core/build-car x)))))

(comment

  (start-ten!)

  )

;; ex 2.4
;; ex 2.5

(defn start-three! []
  (dotimes [_ 3]
    (go
      (time
       (let [part (f/take-part)
             _ (println "took part" part)])))))

(comment

  (start-three!)

  )

;; ex 2.6 - 0:6:40
(defn bottleneck-analysis! []
  (dotimes [_ 3]
    (go
      (time
       (let [x (f/attach-wheel :body :wheel)
             _ (println "attached wheel" x)])))))

(comment

  (bottleneck-analysis!)

  )

;; ex 3.1 - 0:03:18

(def hand-off (chan))

;; ex 3.2 - 0:04:40

(comment

  (<! chan)

  (>! chan 1)

  )

;; ex 3.3 - 0:5:16

(def chan-3-3 (chan))

(comment

  (do
    (go
      (dotimes [_ 5]
        (>! chan-3-3 (rand-int 100))))
    (go
      (while true
        (let [res (<! chan-3-3)
              _ (Thread/sleep 1000)
              _ (println "from chan:" res)]))))

  )

;; ex 3.4 - 0:06:00

(defn gen-random-character []
  (let [alphabet "abcdefghijklmnopqrstuvwxyz"
        rando (rand-int 26)
        res (subs alphabet rando (inc rando))]
    res))

(comment

  (subs "test" 0 1)

  (do
    (def random-chars-chan (chan))
    
    (go
      (dotimes [_ 10]
        (>! random-chars-chan (gen-random-character))))

    (def upper-chars-chan (chan))
    (go
      (while true
        (let [char (<! random-chars-chan)]
          (>! upper-chars-chan (string/upper-case char)))))


    (def printer-chan (chan))
    (go
      (while true
        (let [char (<! upper-chars-chan)
              _ (println "char is:" char)]))))


  )

;; ex 3.5 - 06:46

(defn take-part-fdh [part-pred]
  (loop []
    (let [part (f/take-part)]
      (if (part-pred part)
        part
        (recur)))))


(defn assembly-line [n]
  (let [body-chan (chan)
        _ (go
            (let [body (take-part-fdh f/body?)
                  _ (>! body-chan body)
                  _ (println "put body")]))
        wheel-1-chan (chan)
        _ (go
            (let [wheel (take-part-fdh f/wheel?)
                  _ (>! wheel-1-chan wheel)
                  _ (println "put wheel 1")]))
        wheel-2-chan (chan)
        _ (go
            (let [wheel (take-part-fdh f/wheel?)
                  _ (>! wheel-2-chan wheel)
                  _ (println "put wheel 2")]))
        bw-chan (chan)
        _ (go
            (let [body (<! body-chan)
                  wheel-1 (<! wheel-1-chan)
                  _ (>! bw-chan (f/attach-wheel body wheel-1))
                  _ (println "attached wheel 1")]))
        bww-chan (chan)
        _ (go
            (let [bw (<! bw-chan)
                  wheel-2 (<! wheel-2-chan)
                  _ (>! bww-chan (f/attach-wheel bw wheel-2))
                  _ (println "attached wheel 2")]))
        box-chan (chan)
        _ (go
            (let [bww (<! bww-chan)
                  _ (>! box-chan (f/box-up bww))
                  _ (println "boxed up")]))
        truck-chan (chan)
        _ (go
            (let [box (<! box-chan)
                  _ (>! truck-chan (f/put-in-truck box))
                  _ (println "put on truck")]))]))


(comment

  (assembly-line 0)

  )
