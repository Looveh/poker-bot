(ns poker-bot.logic
  (:require [clojure.set :refer [union difference intersection]]
            [clojure.contrib.combinatorics :refer [combinations]]))

(defn todo []
  (throw (Exception. "TODO")))

(def suits #{:heart :spade :diamond :clubs})

(def ranks #{1 2 3 4 5 6 7 8 9 10 11 12 13})

(def full-deck
  (set (for [suit suits
             rank ranks]
         {:suit suit :rank rank})))

(def all-hands (combinations full-deck 5))

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

(defn seen-cards [game-state]
  (into (:cards-on-hand game-state) (:cards-on-table game-state)))

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

(defn flush? [cards]
  (= 1 (count (distinct (map :suit cards)))))

(defn flush-outs [game-state]
  (let [all-flushes (filter #(flush?) (combinations full-deck 5))
        possible-flushes (filter #(= 4 (intersection % (seen-cards game-state))) all-flushes)
        outs (map #(difference % (intersection % (seen-cards game-state))) possible-flushes)]
    (apply union outs)))

(flush-outs game-state)

(defn straight-outs [game-state]
  (let [all-straights (into #{} (filter straight? all-hands))]
    (map
     #(intersection % (seen-cards game-state))
     all-straights)))

(defn straight? [cards]
  (not-any?
   false?
   (reductions
    #(if (= (inc (:rank %1))
            (:rank %2)) %2 false)
    (sort-by :rank cards))))

(def game-state
  {:cards-on-hand #{{:rank :spades :suit 1} {:rank :spades :suit 2}}
   :cards-on-table #{{:rank :spades :suit 3} {:rank :spades :suit 4} {:rank :spades :suit 5}}})
