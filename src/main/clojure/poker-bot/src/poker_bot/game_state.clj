(ns poker-bot.game-state
  (:gen-class)
  (:use [clojure.tools.logging])
  (:require [clojure.contrib.seq-utils :refer [find-first]])
  (:import (se.cygni.texasholdem.player Player)
           (se.cygni.texasholdem.game Action
                                      Card
                                      Hand
                                      PlayerShowDown
                                      Room
                                      ActionType)
           (se.cygni.texasholdem.game.definitions Rank
                                                  Suit)
           (se.cygni.texasholdem.client PlayerClient
                                        CurrentPlayState)
           (se.cygni.texasholdem.communication.message.request ActionRequest)))

(defn key->ActionType [key]
  (condp = key
    :check ActionType/CHECK
    :call ActionType/CALL
    :fold ActionType/FOLD))

(defn find-action [action-key all-actions]
  (let [action-type (key->ActionType action-key)]
    (find-first
     #(= action-type (.getActionType %))
     all-actions)))

(defn can-respond-with? [action-key all-actions]
  (not (nil? (find-action action-key all-actions))))

(defn- translate-suit [card]
  (let [suit (.getSuit card)]
    (cond (= suit (Suit/CLUBS))    :clubs
          (= suit (Suit/DIAMONDS)) :diamonds
          (= suit (Suit/HEARTS))   :hearts
          (= suit (Suit/SPADES))   :spades)))

;; (defn- translate-suit [card]
;;   (let [suit (.getSuit card)]
;;     (condp = suit
;;       (Suit/CLUBS) :clubs
;;       (Suit/DIAMONDS) :diamonds
;;       (Suit/HEARTS) :hearts
;;       (Suit/SPADES) :spades)))

(defn- translate-rank [card]
  (let [rank (.getRank card)]
    (cond (= rank (Rank/ACE))   1
          (= rank (Rank/DEUCE)) 2
          (= rank (Rank/THREE)) 3
          (= rank (Rank/FOUR))  4
          (= rank (Rank/FIVE))  5
          (= rank (Rank/SIX))   6
          (= rank (Rank/SEVEN)) 7
          (= rank (Rank/EIGHT)) 8
          (= rank (Rank/NINE))  9
          (= rank (Rank/TEN))   10
          (= rank (Rank/JACK))  11
          (= rank (Rank/KING))  12
          (= rank (Rank/QUEEN)) 13)))

;; (defn- translate-rank [card]
;;   (let [rank (.getRank card)]
;;     (condp = rank
;;       (Rank/ACE)   1
;;       (Rank/DEUCE) 2
;;       (Rank/THREE) 3
;;       (Rank/FOUR)  4
;;       (Rank/FIVE)  5
;;       (Rank/SIX)   6
;;       (Rank/SEVEN) 7
;;       (Rank/EIGHT) 8
;;       (Rank/NINE)  9
;;       (Rank/TEN)   10
;;       (Rank/JACK)  11
;;       (Rank/QUEEN) 12
;;       (Rank/KING)) 13))

(defn- java-card->card [card]
  (let [suit (translate-suit card)
        rank (translate-rank card)]
    {:suit suit :rank rank}))

(defn- java-cards->cards [cards]
  (map java-card->card cards))

(defn- get-round [state]
  (let [cards-on-table (.getCommunityCards state)]
    (cond (<= (count cards-on-table) 3) :flop
          (=  (count cards-on-table) 4) :turn
          (=  (count cards-on-table) 5) :river)))

(defn- amount-needed-to-call [request]
  (if-let [call-action (find-action :call (.getPossibleActions request))]
    (.getAmount call-action)
    0))

(defn- can-check? [request]
  (can-respond-with? :check (.getPossibleActions request)))

(defn log-state [state]
  (info (str "Current play state " (.getCurrentPlayState state)))
  (info (str "My cards " (.getMyCards state)))
  (info (str "Cards on table " (.getCommunityCards state))))

(defn get-game-state [client request]
  (let [state (.getCurrentPlayState client)]
    (log-state state)
    {:cards-on-hand (java-cards->cards (.getMyCards state))
     :cards-on-table (java-cards->cards (.getCommunityCards state))
     :pot-amount (.getPotTotal state)
     :call-amount (amount-needed-to-call request)
     :round (get-round state)
     :can-check (can-check? request)}))
