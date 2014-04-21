(ns poker-bot.logic
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

(defn pairs? [cards-on-hand cards-on-table]
  (if (= 1 (count (distinct (map :rank cards-on-hand))))
    true
    (not (zero? (count (filter #(contains? (set (map :rank cards-on-hand))
                                           (:rank %))
                               cards-on-table))))))

;; (def all-hands (map set (combinations full-deck 5)))

;; (def all-flushes (filter flush? all-hands))

;; (def all-straights (filter straight? all-hands))

;; (def all-pairs (filter pairs? (combinations full-deck 2)))

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

;; (defn outs [game-state]
;;   (union (pair-outs game-state)
;;          (straight-outs game-state)
;;          (flush-outs game-state)))

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

(hand-odds {:round :flop
            :cards-on-hand  #{{:rank 7  :suit :hearts}
                         {:rank 4  :suit :hearts}}
       :cards-on-table #{{:rank 10 :suit :hearts}
                         {:rank 11 :suit :clubs}
                         {:rank 1  :suit :hearts}}})

(defn action [game-state]
  (if (:can-check game-state)
    :check
    (let [pot-odds (pot-odds game-state)
          hand-odds (hand-odds game-state)]
      (info (str game-state))
      (info (str "Odds: " pot-odds ":" hand-odds))
      (if (winning-hand? (:cards-on-hand game-state) (:cards-on-table game-state))
        (do
          (info "Winning hand.")
          :call)
        (if (>= pot-odds hand-odds)
          :call
          :fold)))))
