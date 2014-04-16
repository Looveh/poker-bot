(ns poker-bot.game-state
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

(defn key->action [key]
  (cond (= key :check) ActionType/CHECK
        (= key :call)  ActionType/CALL
        (= key :fold)  ActionType/FOLD))

(defn- translate-suit [card]
  (let [suit (.getSuit card)]
    (cond (= suit (Suit/CLUBS))    :clubs
          (= suit (Suit/DIAMONDS)) :diamonds
          (= suit (Suit/HEARTS))   :hearts
          (= suit (Suit/SPADES))   :spades)))

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

(defn- java-card->card [card]
  (let [suit (translate-suit card)
        rank (translate-rank card)]
    {:suit suit :rank rank}))

(defn- java-cards->cards [cards]
  (map java-card->card cards))

(defn- get-round [state]
  (let [cards-on-table (.getCommunityCards state)]
    (cond (<= cards-on-table 3) :flop
          (=  cards-on-table 4) :turn
          (=  cards-on-table 5) :river)))

(defn- find-first [f coll]
  (first (filter f coll)))

(defn- amount-needed-to-call [state]
  (let [call-action (find-first #(= % ActionType/CALL))]
    (if (nil? call-action)
      0
      (.getAmount call-action))))

(defn get-game-state [client request]
  (let [state (.getCurrentPlayState client)]
    {:cards-on-hand (java-cards->cards (.getMyCards state))
     :cards-on-table (java-cards->cards (.getCommunityCards state))
     :pot-amount (.getPotTotal state)
     :call-amount (amount-needed-to-call state)
     :round (get-round state)}))

