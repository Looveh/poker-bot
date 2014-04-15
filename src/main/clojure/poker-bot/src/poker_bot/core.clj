(ns poker-bot.core
  (:gen-class))
(import 'se.cygni.texasholdem.player.Player)

(declare get-bot)
(declare get-best-action)

(defn -main
  []
  (.playATrainingGame
   (poker-bot
    "poker.cygni.se"
    4711)))

(defn get-bot
  [host port]
  (proxy [Player] []
    (getName
     []
     "ClojureBot")
    (actionRequired
     [request]
     (get-best-action request))
    (onPlayIsStarted
     [event]
     )
    (onTableChangedStateEvent
     [event]
     )
    (onYouHaveBeenDealtACard
     [event]
     )
    (onCommunityHasBeenDealtACard
     [event]
     )
    (onPlayerBetBigBlind
     [event]
     )
    (onPlayerBetSmallBlind
     [event]
     )
    (onPlayerFolded
     [event]
     )
    (onPlayerForcedFolded
     [event]
     )
    (onPlayerCalled
     [event]
     )
    (onPlayerRaised
     [event]
     )
    (onTableIsDone
     [event]
     )
    (onPlayerChecked
     [event]
     )
    (onYouWonAmount
     [event]
     )
    (onShowDown
     [event]
     )
    (onPlayerQuit
     [event]
     )
    (connectionToGameServerLost
     [event]
     )
    (connectionToGameServerEstablished
     [event]
     )
    (serverIsShuttingDown
     [event]
     )
    ))

(defn get-best-action
  [request]
  (first
   (.getPossibleActions
    request)))
