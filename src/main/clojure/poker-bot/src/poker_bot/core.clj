(ns poker-bot.core
  (:gen-class))
(import 'se.cygni.texasholdem.player.Player)

(declare get-bot)
(declare get-best-action)

(def bot (atom nil))
(def client (atom nil))
(def host "poker.cygni.se")
(def port 4711)

(defn -main
  []
  (swap!
   bot
   (get-bot host port))
  (swap!
   client
   (PlayerClient. @bot host port))
  (play-training-game
   @client))

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

(play-training-game
 [client]
 (.connect client)
 (.registerForPlay Room/TRAINING))
