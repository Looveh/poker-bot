(ns poker-bot.core
  (:gen-class)
  (:import
   (se.cygni.texasholdem.player
    Player)
   (se.cygni.texasholdem.game
    Action
    Card
    Hand
    PlayerShowDown
    Room)
   (se.cygni.texasholdem.client
    PlayerClient
    CurrentPlayState)
   (se.cygni.texasholdem.communication.message.request
    ActionRequest)
   (se.cygni.texasholdem.game.definitions
    PlayState
    PokerHand)
   (se.cygni.texasholdem.game.util
    PokerHandUtil)
   (se.cygni.texasholdem.communication.message.event
    CommunityHasBeenDealtACardEvent
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

(declare get-bot)
(declare get-best-action)
(declare play-training-game)

(def bot (atom nil))
(def client (atom nil))
(def host "poker.cygni.se")
(def port 4711)

(defn -main
  []
  (do
    (reset!
     bot
     (get-bot host port))
    (reset!
     client
     (PlayerClient. @bot host port))
    (play-training-game @client)))

(defn get-bot [host port]
  (proxy [Player] []
    (getName
     []
     "ClojureBot")
    (actionRequired
     [request]
     (get-best-action request))
    (onPlayIsStarted
     [event]
     (println "Play started")
     )
    (onTableChangedStateEvent
     [event]
     (println "Table changed state")
     )
    (onYouHaveBeenDealtACard
     [event]
     (println "We were dealt a card")
     )
    (onCommunityHasBeenDealtACard
     [event]
     (println "Community has been dealt a card")
     )
    (onPlayerBetBigBlind
     [event]
     (println "Player bet big blind")
     )
    (onPlayerBetSmallBlind
     [event]
     (println "Player bet small blind")
     )
    (onPlayerFolded
     [event]
     (println "Player folded")
     )
    (onPlayerForcedFolded
     [event]
     (println "Player forced to fold")
     )
    (onPlayerCalled
     [event]
     (println "Player called")
     )
    (onPlayerRaised
     [event]
     (println "Player raised")
     )
    (onTableIsDone
     [event]
     (println "Table is done")
     )
    (onPlayerChecked
     [event]
     (println "Player forced to fold")
     )
    (onYouWonAmount
     [event]
     (println "We won an amount")
     )
    (onShowDown
     [event]
     (println "Show Down!")
     )
    (onPlayerQuit
     [event]
     (println "Player quit!")
     )
    (connectionToGameServerLost
     []
     (println "Connection to game server lost")
     (System/exit 0)
     )
    (connectionToGameServerEstablished
     []
     (println "Connection to game server established")
     )
    (serverIsShuttingDown
     [event]
     (println "Server shutting down")
     )))

(defn get-best-action
  [request]
  (first
   (.getPossibleActions
    request)))

(defn play-training-game
  [client]
  (.connect client)
  (.registerForPlay client Room/TRAINING))
