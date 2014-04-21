(ns poker-bot.logic
  (:gen-class)
  (:use [clojure.tools.logging])
  (:require [clojure.set :refer [union difference intersection]]
            [clojure.contrib.combinatorics :refer [combinations]]
            [clojure.contrib.seq-utils :refer [find-first]]))

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

(defn pairs? [cards]
  (not (= (count cards)
          (count (distinct (map :rank cards))))))

(defn seen-cards [game-state]
  (into (:cards-on-hand game-state) (:cards-on-table game-state)))

(defn unseen-cards [game-state]
  (difference full-deck (seen-cards game-state)))

(defn winning-hand? [cards]
  (or (pairs? cards)
      (straight? cards)
      (flush? cards)))

(defn outs [game-state]
  (let [cards (seen-cards game-state)
        unseen-cards (unseen-cards game-state)]
    (filter #(winning-hand? (conj cards %)) unseen-cards)))

(defn pot-odds [game-state]
  (let [pot-amount (:pot-amount game-state)
        call-amount (:call-amount game-state)]
    (if (zero? call-amount)
      1000
      (/ (+ pot-amount call-amount) call-amount))))

(defn hand-odds [game-state]
  (let [round (:round game-state)
        odds (dec (/ (count (unseen-cards game-state))
                     (count (outs game-state))))]
    (cond (= :flop round) (* odds 4)
          (= :turn round) (* odds 2)
          :else odds)))

(defn action [game-state]
  (let [pot-odds (pot-odds game-state)
        hand-odds (hand-odds game-state)]
    (info (str game-state))
    (info (str "Odds: " pot-odds ":" hand-odds))
    (if (:can-check game-state)
      :check
      (if (winning-hand? (seen-cards game-state))
        (do
          (info "Winning hand!")
          :call)
        (if (>= pot-odds hand-odds)
          :call
          (if (and (:is-small-blind game-state) (= :pre-flop (:round game-state)))
            :call
            :fold))))))

(defn stuff [game-state]
  (and (:is-small-blind game-state) (= :pre-flop (:round game-state))))

(stuff {:is-small-blind true
        :round :pre-flop})
