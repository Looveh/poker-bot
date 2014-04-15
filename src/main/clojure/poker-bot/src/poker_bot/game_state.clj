(ns poker-bot.game-state
  (:import (se.cygni.texasholdem.player Player)
           (se.cygni.texasholdem.game Action
                                      Card
                                      Hand
                                      PlayerShowDown
                                      Room
                                      ActionType)
           (se.cygni.texasholdem.client PlayerClient
                                        CurrentPlayState)
           (se.cygni.texasholdem.communication.message.request ActionRequest)))

(defn key->action [key]
  (throw (Exception. "TODO")))

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

(defn- java-card->card [card]
  (throw (Exception. "TODO")))

(defn- java-cards->cards [cards]
  (map java-card->card cards))

(defn get-game-state [client request]
  (let [state (.getCurrentPlayState client)]
    {:cards-on-hand (java-cards->cards (.getMyCards state))
     :cards-on-table (java-cards->cards (.getCommunityCards state))
     :pot-amount (.getPotTotal state)
     :call-amount (amount-needed-to-call state)
     :round (get-round state)}))

