(defn todo []
  (throw (Exception. "TODO")))

(def suits #{:heart :spade :diamond :clubs})

(def ranks #{:ace :two :three :four :five :six :seven
             :eight :nine :ten :jack :queen :king})

(def full-deck
  (set (for [suit suits
        rank ranks]
    {:suit suit :rank rank})))

(defn action [game-state]
  (if (can-check? game-state)
    :check
    (if (<= (pot-odds game-state) (hand-odds game-state))
      :fold
      :call)))

(defn pot-odds [game-state]
  (/ 1 (+ (pot-size game-state)
          (amount-needed-to-call game-state))))

(defn hand-odds [game-state]
  (let [odds (dec (/ (number-of-unseen-cards game-state)
                  (number-of-outs game-state)))]
    (if (flop? game-state)
      (* odds 4)
      (if (turn? game-state)
        (* odds 2)
        odds))))

(defn can-check? [game-state]
  (todo))

(defn flop? [game-state]
  (todo))

(defn turn? [game-state]
  (todo))

(defn cards-on-hand [game-state]
  (todo))

(defn cards-on-table [game-state]
  (todo))

(defn number-of-unseen-cards [game-state]
  (let [seen-cards (into (cards-on-hand) (cards-on-table))
        unseen-cards (clojure.set/difference full-deck seen-cards)])
    (count unseen-cards))

(defn number-of-outs [game-state]
  (todo))

(defn pot-size [game-state]
  (todo))

(defn amount-needed-to-call [game-state]
  (todo))

