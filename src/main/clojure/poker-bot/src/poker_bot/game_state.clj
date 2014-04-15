(ns poker-bot.game-state
  (:import
   (se.cygni.texasholdem.player
    Player)
   (se.cygni.texasholdem.game
    Action
    Card
    Hand
    PlayerShowDown
    Room
    ActionType)
   (se.cygni.texasholdem.client
    PlayerClient
    CurrentPlayState)
   (se.cygni.texasholdem.communication.message.request
    ActionRequest)))

(declare get-game-state)
(declare get-round)
(declare can-check?)
(declare flop?)
(declare amount-needed-to-call)
(declare find-first)

(defn get-game-state [client request]
  (let [state (.getCurrentPlayState client)]
    {:cards-on-hand (.getMyCards state)
     :cards-on-table (.getCommunityCards state)
     :pot-amount (.getPotTotal state)
     :turn-type (get-round state)
     :call-amount (amount-needed-to-call state)
     :possible-actions (.getPossibleActions request)}))

(defn get-round [state]
  (let [cards-on-table (.getCommunityCards state)]
    (cond
     (= cards-on-table 0) :flop
     (= cards-on-table 0) :turn
     (= cards-on-table 0) :river)))

(defn can-check? [game-state]
  (contains?
   (:possible-actions game-state)
    ActionType/CHECK))

(defn flop? [game-state]
  (= :flop (:turn-type game-state)))

(defn amount-needed-to-call [state]
  (let [call-action (find-first #(= % ActionType/CALL))]
    (if (nil? call-action)
      0
      (.getAmount call-action))))

(defn find-first [f coll]
  (first (filter f coll)))
