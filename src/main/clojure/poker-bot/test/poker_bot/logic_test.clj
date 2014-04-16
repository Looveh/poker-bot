(ns poker-bot.logic-test
  (:require [poker-bot.logic :refer :all]
            [expectations :refer :all]))

(expect #{{:rank 7  :suit :hearts}
          {:rank 4  :suit :hearts}
          {:rank 10 :suit :hearts}
          {:rank 11 :suit :clubs}
          {:rank 1  :suit :hearts}}
        (seen-cards {:cards-on-hand  #{{:rank 7  :suit :hearts}
                                       {:rank 4  :suit :hearts}}
                     :cards-on-table #{{:rank 10 :suit :hearts}
                                       {:rank 11 :suit :clubs}
                                       {:rank 1  :suit :hearts}}}))

(expect true
        (flush? #{{:rank 1  :suit :clubs}
                  {:rank 2  :suit :clubs}
                  {:rank 5  :suit :clubs}
                  {:rank 13 :suit :clubs}
                  {:rank 8  :suit :clubs}}))

(expect false
        (flush? #{{:rank 1  :suit :clubs}
                  {:rank 2  :suit :clubs}
                  {:rank 5  :suit :hearts}
                  {:rank 13 :suit :clubs}
                  {:rank 8  :suit :clubs}}))

(expect true
        (straight? #{{:rank 3  :suit :clubs}
                     {:rank 2  :suit :spades}
                     {:rank 5  :suit :hearts}
                     {:rank 4  :suit :diamonds}
                     {:rank 6  :suit :clubs}}))

(expect false
        (straight? #{{:rank 1  :suit :clubs}
                     {:rank 2  :suit :spades}
                     {:rank 5  :suit :hearts}
                     {:rank 13 :suit :diamonds}
                     {:rank 8  :suit :clubs}}))

(expect false
        (pairs? #{{:rank 1  :suit :clubs}
                  {:rank 2  :suit :spades}
                  {:rank 5  :suit :hearts}
                  {:rank 13 :suit :diamonds}
                  {:rank 8  :suit :clubs}}))

(expect true
        (pairs? #{{:rank 1  :suit :clubs}
                  {:rank 2  :suit :spades}
                  {:rank 2  :suit :hearts}
                  {:rank 13 :suit :diamonds}
                  {:rank 8  :suit :clubs}}))

(expect true
        (pairs? #{{:rank 1  :suit :clubs}
                  {:rank 2  :suit :spades}
                  {:rank 2  :suit :hearts}
                  {:rank 8 :suit :diamonds}
                  {:rank 8  :suit :clubs}}))

(expect true
        (pairs? #{{:rank 1  :suit :clubs}
                  {:rank 2  :suit :spades}
                  {:rank 2  :suit :hearts}
                  {:rank 2 :suit :diamonds}
                  {:rank 8  :suit :clubs}}))

(expect true
        (pairs? #{{:rank 2  :suit :spades}
                  {:rank 2  :suit :hearts}}))

(expect false
        (pairs? #{{:rank 3  :suit :spades}
                  {:rank 2  :suit :hearts}}))

(expect (/ 1 5)
        (pot-odds {:pot-amount  4
                   :call-amount 1}))

(expect (/ 3 7)
        (pot-odds {:pot-amount  4
                   :call-amount 3}))

;; (expect #{{:rank 2 :suit :clubs}
;;           {:rank 2 :suit :diamonds}
;;           {:rank 2 :suit :spades}
;;           {:rank 7 :suit :clubs}
;;           {:rank 7 :suit :diamonds}
;;           {:rank 7 :suit :spades}}
;;         (straight-outs {:cards-on-hand  #{{:rank 3  :suit :hearts}
;;                                           {:rank 4  :suit :hearts}}
;;                         :cards-on-table #{{:rank 5 :suit :hearts}
;;                                           {:rank 6 :suit :clubs}
;;                                           {:rank 12  :suit :hearts}}}))



;; (expect #{{:rank 2  :suit :hearts}
;;           {:rank 4  :suit :hearts}
;;           {:rank 5  :suit :hearts}
;;           {:rank 8  :suit :hearts}
;;           {:rank 9  :suit :hearts}
;;           {:rank 10 :suit :hearts}
;;           {:rank 11 :suit :hearts}
;;           {:rank 12 :suit :hearts}
;;           {:rank 13 :suit :hearts}}
;;         (flush-outs {:cards-on-hand  #{{:rank 1  :suit :hearts}
;;                                        {:rank 3  :suit :hearts}}
;;                      :cards-on-table #{{:rank 7  :suit :hearts}
;;                                        {:rank 6  :suit :hearts}
;;                                        {:rank 1  :suit :clubs}}}))


;; (expect #{{:rank 7 :suit :spades}
;;           {:rank 7 :suit :clubs}
;;           {:rank 7 :suit :diamonds}
;;           {:rank 4 :suit :spades}
;;           {:rank 4 :suit :clubs}
;;           {:rank 4 :suit :diamonds}}
;;         (pair-outs {:cards-on-hand  #{{:rank 7  :suit :hearts}
;;                                       {:rank 4  :suit :hearts}}
;;                     :cards-on-table #{{:rank 10 :suit :hearts}
;;                                       {:rank 11 :suit :clubs}
;;                                       {:rank 1  :suit :hearts}}}))

;; (expect (hand-odds {:cards-on-hand  #{{:rank 7  :suit :hearts}
;;                                       {:rank 4  :suit :hearts}}
;;                     :cards-on-table #{{:rank 10 :suit :hearts}
;;                                       {:rank 11 :suit :clubs}
;;                                       {:rank 1  :suit :hearts}}})
;;         (/ 47 9))
