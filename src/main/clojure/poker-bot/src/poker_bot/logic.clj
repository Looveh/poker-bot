(ns poker-bot.logic
  (:require [clojure.set :refer [union difference]]
            [clojure.contrib.combinatorics :refer [combinations]]))

(defn todo []
  (throw (Exception. "TODO")))

(def suits #{:heart :spade :diamond :clubs})

(def ranks #{:ace :two :three :four :five :six :seven
             :eight :nine :ten :jack :queen :king})

(def full-deck
  (set (for [suit suits
             rank ranks]
         {:suit suit :rank rank})))

(defn action [game-state]
  (if (can-check? game-state)
    :check
    (if (<= (pot-odds game-state) (hand-odds game-state))
      :fold
      :call)))

(defn pot-odds [game-state]
  (/ 1 (+ (pot-size game-state)
          (amount-needed-to-call game-state))))

(defn hand-odds [game-state]
  (let [odds (dec (/ (count (unseen-cards game-state))
                     (count (outs game-state))))]
    (if (flop? game-state)
      (* odds 4)
      (if (turn? game-state)
        (* odds 2)
        odds))))

(defn can-check? [game-state]
  (todo))

(defn flop? [game-state]
  (todo))

(defn turn? [game-state]
  (todo))

(defn cards-on-hand [game-state]
  (todo))

(defn cards-on-table [game-state]
  (todo))

(defn seen-cards [game-state]
  (into (cards-on-hand game-state) (cards-on-table game-state)))

(defn unseen-cards [game-state]
  (difference full-deck (seen-cards game-state)))

(defn outs [game-state]
  (union (pair-outs game-state)
                            (straight-outs game-state)
                            (flush-outs game-state)))

(defn pair-outs [game-state]
  (filter #(contains? (vals (cards-on-hand game-state))
                      (:rank %))
          unseen-cards))

(defn straight-outs [game-state]
  (let [possible-straights (filter #() (combinations full-deck 5))]
    ))

(defn flush-outs [game-state]
  (todo))

(defn pot-size [game-state]
  (todo))

(defn amount-needed-to-call [game-state]
  (todo))

