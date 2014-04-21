(ns poker-bot.core
  (:gen-class)
  (:use [clojure.tools.logging])
  (:require [poker-bot.game-state :refer [get-game-state key->ActionType find-action]]
            [poker-bot.logic :refer [action]]
            [clojure.repl :refer [pst]]
            [clojure.contrib.seq-utils :refer [find-first]])
  (:import
   (se.cygni.texasholdem.player Player)
   (se.cygni.texasholdem.game Action
                              Card
                              Hand
                              PlayerShowDown
                              Room)
   (se.cygni.texasholdem.client PlayerClient
                                CurrentPlayState)
   (se.cygni.texasholdem.communication.message.request ActionRequest)
   (se.cygni.texasholdem.game.definitions PlayState
                                          PokerHand)
   (se.cygni.texasholdem.game.util PokerHandUtil)
   (se.cygni.texasholdem.communication.message.event CommunityHasBeenDealtACardEvent
                                                     PlayIsStartedEvent
                                                     PlayerBetBigBlindEvent
                                                     PlayerBetSmallBlindEvent
                                                     PlayerCalledEvent
                                                     PlayerCheckedEvent
                                                     PlayerFoldedEvent
                                                     PlayerForcedFoldedEvent
                                                     PlayerQuitEvent
                                                     PlayerRaisedEvent
                                                     PlayerWentAllInEvent
                                                     ServerIsShuttingDownEvent
                                                     ShowDownEvent
                                                     TableChangedStateEvent
                                                     TableIsDoneEvent
                                                     YouHaveBeenDealtACardEvent
                                                     YouWonAmountEvent)))

(def client (atom nil))
(def host "poker.cygni.se")
(def port 4711)

(defn- get-best-action [request]
  (info (str "Possible actions to request: " (.getPossibleActions request)))
  (let [game-state (get-game-state @client request)
        possible-actions (.getPossibleActions request)]
    (info (str "Action requested, game state: " game-state))
    (if-let [best-action (find-action (action game-state) possible-actions)]
      best-action
      (find-action :fold possible-actions))))

(defn get-action [request]
  (try
    (get-best-action request)
    (catch Exception e (info (str "caught exception: " (pst e))))))

(defn- play-training-game [client]
  (.connect client)
  (.registerForPlay client Room/TRAINING))

(defn- get-bot [host port]
  (proxy [Player] []
    (getName []
             "ClojureBot")
    (actionRequired
     [request]
     (let [action (get-action request)]
       (info (str "Request response: " action))
       action))
    (onPlayIsStarted [event]
                     (info "Play started"))
    (onTableChangedStateEvent [event]
                              ;(info "Table changed state")
                              )
    (onYouHaveBeenDealtACard [event]
                             ;(info "We were dealt a card")
                             )
    (onCommunityHasBeenDealtACard [event]
                                  ;(info "Community has been dealt a card")
                                  )
    (onPlayerBetBigBlind [event]
                         ;(info "Player bet big blind")
                         )
    (onPlayerBetSmallBlind [event]
                           ;(info "Player bet small blind")
                           )
    (onPlayerFolded [event]
                    ;(info "Player folded")
                    )
    (onPlayerForcedFolded [event]
                          ;(info "Player forced to fold")
                          )
    (onPlayerCalled [event]
                    ;(info "Player called")
                    )
    (onPlayerRaised [event]
                    ;(info "Player raised")
                    )
    (onTableIsDone [event]
                   ;(info "Table is done")
                   )
    (onPlayerChecked [event]
                     ;(info "Player forced to fold")
                     )
    (onYouWonAmount [event]
                    ;(info "We won an amount")
                    )
    (onShowDown [event]
                ;(info "Show Down!")
                )
    (onPlayerQuit [event]
                  ;(info "Player quit!")
                  )
    (connectionToGameServerLost
     []
     (info "Connection to game server lost")
     (System/exit 0))
    (connectionToGameServerEstablished [])
    (serverIsShuttingDown [event])
    (onPlayerWentAllIn [event])))

(defn -main []
  (do
    (reset! client (PlayerClient. (get-bot host port) host port))
    (play-training-game @client)))
