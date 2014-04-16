(ns poker-bot.logic
  (:require [clojure.set :refer [union difference intersection]]
            [clojure.contrib.combinatorics :refer [combinations]]))

(def suits #{:hearts :spades :diamonds :clubs})

(def ranks #{1 2 3 4 5 6 7 8 9 10 11 12 13})

(def full-deck (set (for [suit suits
                          rank ranks]
                      {:suit suit :rank rank})))

(defn flush? [cards]
  (= 1 (count (distinct (map :suit cards)))))

(defn straight? [cards]
  (not-any? false? (reductions #(if (= (inc (:rank %1)) (:rank %2))
                                  %2
                                  false)
                               (sort-by :rank cards))))

(def all-hands (combinations full-deck 5))

(def all-flushes (filter flush? all-hands))

(def all-straights (filter straight? all-hands))

(defn seen-cards [game-state]
  (into (:cards-on-hand game-state) (:cards-on-table game-state)))

(defn unseen-cards [game-state]
  (difference full-deck (seen-cards game-state)))

(defn pair-outs [game-state]
  (filter #(contains? (vals (:cards-on-hand game-state))
                      (:rank %))
          unseen-cards))

(defn flush-out [flush hand]
  (let [flushables (intersection flush hand)]
    (difference flushables flush)))

(defn flush-outs [game-state]
  (let [possible-flushes (filter #(= 4 (intersection % (seen-cards game-state))) all-flushes)]
    (map flush-out possible-flushes)))

(defn straight-outs [game-state]
  (let [all-straights (into #{} (filter straight? all-hands))]
    (map #(intersection % (seen-cards game-state))
         all-straights)))

(defn outs [game-state]
  (union (pair-outs game-state)
         (straight-outs game-state)
         (flush-outs game-state)))

(defn pot-odds [game-state]
  (/ 1 (+ (:pot-amount game-state)
          (:call-amount game-state))))

(defn hand-odds [game-state]
  (let [round (:round game-state)
        odds (dec (/ (count (unseen-cards game-state))
                     (count (outs game-state))))]
    (cond (= :flop round) (* odds 4)
          (= :turn round) (* odds 2)
          :else odds)))

(defn action [game-state]
  (if (= 0 (:call-amount game-state))
    :check
    (if (<= (pot-odds game-state) (hand-odds game-state))
      :fold
      :call)))
