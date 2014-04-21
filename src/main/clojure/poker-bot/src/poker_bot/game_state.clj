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
    ({(Suit/CLUBS) :clubs
      (Suit/DIAMONDS) :diamond
      (Suit/HEARTS) :hearts
      (Suit/SPADES) :spades}
      suit)))

(defn- translate-rank [card]
  (let [rank (.getRank card)]
    ({(Rank/ACE) 1
      (Rank/DEUCE) 2
      (Rank/THREE) 3
      (Rank/FOUR) 4
      (Rank/FIVE) 5
      (Rank/SIX) 6
      (Rank/SEVEN) 7
      (Rank/EIGHT) 8
      (Rank/NINE) 9
      (Rank/TEN) 10
      (Rank/JACK) 11
      (Rank/KING) 12
      (Rank/QUEEN) 13}
      rank)))

(defn- java-card->card [card]
  (let [suit (translate-suit card)
        rank (translate-rank card)]
    {:suit suit :rank rank}))

(defn- java-cards->cards [cards]
  (map java-card->card cards))

(defn- get-round [state]
  (let [ordinal (.ordinal (.getCurrentPlayState state))]
    ({0 :pre-flop
      1 :flop
      2 :turn
      3 :river}
        ordinal)))

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
     :is-big-blind (.amIBigBlindPlayer state)
     :is-small-blind (.amISmallBlindPlayer state)
     :pot-amount (.getPotTotal state)
     :call-amount (amount-needed-to-call request)
     :round (get-round state)
     :can-check (can-check? request)}))
