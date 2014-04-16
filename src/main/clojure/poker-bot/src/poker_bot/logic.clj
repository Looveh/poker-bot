(ns poker-bot.logic
  (:require [clojure.set :refer [union difference intersection]]
            [clojure.contrib.combinatorics :refer [combinations]]
            [clojure.contrib.seq-utils :refer [find-first]]))

(def suits #{:hearts :spades :diamonds :clubs})

(def ranks #{1 2 3 4 5 6 7 8 9 10 11 12 13})

(def full-deck (set (for [suit suits
                          rank ranks]
                      {:suit suit :rank rank})))

full-deck

(defn flush? [cards]
  (= 1 (count (distinct (map :suit cards)))))

(defn straight? [cards]
  (not-any? false? (reductions #(if (= (inc (:rank %1)) (:rank %2))
                                  %2
                                  false)
                               (sort-by :rank cards))))

(defn pairs? [cards]
  (not (= (count cards)
          (count (distinct (map :rank cards))))))

(def all-hands (map set (combinations full-deck 5)))

(def all-flushes (filter flush? all-hands))

(def all-straights (filter straight? all-hands))

(def all-pairs (filter pairs? (combinations full-deck 2)))

(defn seen-cards [game-state]
  (into (:cards-on-hand game-state) (:cards-on-table game-state)))

(defn unseen-cards [game-state]
  (difference full-deck (seen-cards game-state)))

(defn pair-outs [game-state]
  (set (filter #(contains? (set (map :rank (:cards-on-hand game-state)))
                      (:rank %))
           (unseen-cards game-state))))

(defn flush-outs [game-state]
  (let [cards (seen-cards game-state)
        flush-suit (first (find-first #(>= (second %) 4)
                                      (map #(list (key %) (count (val %)))
                                           (group-by :suit cards))))
        cards-with-suit (filter #(= flush-suit (:suit %)) full-deck)]
    (set (filter #(not (contains? cards %)) cards-with-suit))))

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
      :call
      :fold)))
