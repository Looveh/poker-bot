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

(defn flush? [cards]
  (let [cards-in-flush (filter #(<= 5 (count (val %))) (group-by :suit cards))]
    (not (empty? cards-in-flush))))

(defn straight? [cards]
  (not-any? false? (reductions #(if (= (inc (:rank %1)) (:rank %2))
                                  %2
                                  false)
                               (sort-by :rank cards))))

(defn pairs? [cards-on-hand cards-on-table]
  (if (= 1 (count (distinct (map :rank cards-on-hand))))
    true
    (not (zero? (count (filter #(contains? (set (map :rank cards-on-hand))
                                           (:rank %))
                               cards-on-table))))))

(defn seen-cards [game-state]
  (into (:cards-on-hand game-state) (:cards-on-table game-state)))

(defn unseen-cards [game-state]
  (difference full-deck (seen-cards game-state)))

;; (defn pair-outs [game-state]
;;   (set (filter #(contains? (set (map :rank (:cards-on-hand game-state)))
;;                       (:rank %))
;;            (unseen-cards game-state))))

;; (defn flush-outs [game-state]
;;   (let [cards (seen-cards game-state)
;;         flush-suit (first (find-first #(>= (second %) 4)
;;                                       (map #(list (key %) (count (val %)))
;;                                            (group-by :suit cards))))
;;         cards-with-suit (filter #(= flush-suit (:suit %)) full-deck)]
;;     (set (filter #(not (contains? cards %)) cards-with-suit))))

;; (defn straight-outs [game-state]
;;   (let [all-straights (into #{} (filter straight? all-hands))]
;;     (map #(intersection % (seen-cards game-state))
;;          all-straights)))


(defn winning-hand? [cards-on-hand cards-on-table]
  (or (pairs? cards-on-hand cards-on-table)
      (straight? (concat cards-on-hand cards-on-table))
      (flush? (concat cards-on-hand cards-on-table))))

(defn outs [game-state]
  (let [cards (seen-cards game-state)
        unseen-cards (unseen-cards game-state)]
    (filter #(winning-hand? (:cards-on-hand game-state) (conj (:cards-on-table game-state) %)) unseen-cards)))

(defn pot-odds [game-state]
  (let [pot-amount (:pot-amount game-state)
        call-amount (:call-amount game-state)]
    (if (zero? call-amount)
      0
      (/ (+ pot-amount call-amount) call-amount))))

(defn hand-odds [game-state]
  (let [round (:round game-state)
        odds (dec (/ (count (unseen-cards game-state))
                     (count (outs game-state))))]
    (cond (= :flop round) (* odds 4)
          (= :turn round) (* odds 2)
          :else odds)))

(defn all-in? [game-state]
  (let [cards (concat (:cards-on-hand game-state) (:cards-on-table game-state))]
    (or (straight? cards)
        (flush? cards))))

(defn should-continue-to-flop? [game-state]
  (let [cards-on-hand (:cards-on-hand game-state)]
    (or (:can-check game-state)
        (:is-big-blind game-state)
        (:is-small-blind game-state)
        (pairs? cards-on-hand #{})
        (and (not (zero? (count (group-by :suit cards-on-hand))))
             (= 2 (count (filter #(or (= 12 (:rank %))
                                             (= 13 (:rank %))
                                             (= 1 (:rank %)))
                                        cards-on-hand)))))))

(defn action [game-state]
  (if (= :pre-flop (:round game-state))
    (if (should-continue-to-flop? game-state)
      :call
      :fold)
    (if (and (< 2000 (:call-amount game-state))
             (not (all-in? game-state)))
      :fold
      (let [pot-odds (pot-odds game-state)
            hand-odds (hand-odds game-state)]
        (info (str game-state))
        (info (str "Odds: " pot-odds ":" hand-odds))
        (if (:can-check game-state)
          :check
          (if (winning-hand? (:cards-on-hand game-state) (:cards-on-table game-state))
            (do
              (info "Winning hand!")
              :raise)
            (if (<= pot-odds hand-odds)
              :call
              (if (= :pre-flop (:round game-state))
                :call
                (if (all-in? game-state)
                  :all-in
                  (if (> 150 (:call-amount game-state))
                    :call
                    :fold))))))))))

(defn stuff [game-state]
  (and (:is-small-blind game-state) (= :pre-flop (:round game-state))))

(stuff {:is-small-blind true
        :round :pre-flop})
