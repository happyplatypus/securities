(ns securities.index
  (:require

 [net.cgrand.enlive-html :as enlive]
             [clojure.java.io :as io]
             [clojure.string :as str]
             [incanter.zoo :as zoo]
             [clj-time.format :as tf]
             [clj-time.core :as tt]
             [clj-time.predicates :as pr]
             [clj-time.local :as l]
             [clojure.java.io :as io]
             [clojure.string :as str]
             [clj-http.client :as client]
             [structure.ring-buffer :as rb]
  [postal.core :as postal]

		[clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]
             [incanter [core :refer [$]
                             :as incanter$]
                       [core :as incanter]
                       [stats :as stats]
                       [io :as io2]
                       [charts :as charts]
                       [datasets :as dataset]]

             [me.raynes.conch :refer [programs with-programs let-programs] :as sh]

             )





  (:gen-class))



(def mc-cutoff 800E6)
(def price-cutoff 5)
(def adv-cutoff-millions 5)

(def data (io2/read-dataset "http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=NASDAQ&render=download" :header true))


(def nasdaq (incanter/to-dataset data))

(def symbols (incanter$/$ :Symbol nasdaq ))
(def name_ (incanter$/$ :Name nasdaq ))
(def lastsale (incanter$/$ :LastSale nasdaq ))
(def market-cap (incanter$/$ :MarketCap nasdaq ))
;(println lastsale)

(def filter-data0 (map vector symbols name_ lastsale market-cap))

(def filter-data1 (filter #(number? (nth % 2)) filter-data0))

(def filter-data2-nasdaq (filter #(and (>= (nth % 2) price-cutoff) (>= (nth % 3) mc-cutoff)) filter-data1))



(def data (io2/read-dataset "http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=NYSE&render=download" :header true))


(def nasdaq (incanter/to-dataset data))

(def symbols (incanter$/$ :Symbol nasdaq ))
(def name_ (incanter$/$ :Name nasdaq ))
(def lastsale (incanter$/$ :LastSale nasdaq ))
(def market-cap (incanter$/$ :MarketCap nasdaq ))
(println lastsale)

(def filter-data0 (map vector symbols name_ lastsale market-cap))

(def filter-data1 (filter #(number? (nth % 2)) filter-data0))

(def filter-data2-nyse (filter #(and (>= (nth % 2) price-cutoff) (>= (nth % 3) mc-cutoff)) filter-data1))
















;;;;;;;; functions around date decision

(defn formatlocal [n offset]
  (let [nlocal (tt/to-time-zone n (tt/time-zone-for-offset offset))]
    (tf/unparse (tf/formatter-local "yyyy-MM-dd hh:mm:ss aa")
               nlocal)))

(
defn currentTime[]
(formatlocal (tt/now) -5)
)


(def tt_date_  (tt/date-time (read-string (subs (currentTime) 0 4)) (. Integer parseInt (subs (currentTime) 5 7)) (. Integer parseInt (subs (currentTime) 8 10))))


(println "today is.." )
(println tt_date_)
(def tt_date_1 (tt/minus tt_date_ (tt/days 1) ))
(def tt_date_2 (tt/minus tt_date_ (tt/days 2) ))
(def tt_date_3 (tt/minus tt_date_ (tt/days 3) ))
(def tt_date_4 (tt/minus tt_date_ (tt/days 4) ))
(def tt_date_5 (tt/minus tt_date_ (tt/days 5) ))

(def tt_date_14 (tt/minus tt_date_ (tt/days 14) ))


(defn pad_[x] (if (= 2 (count (str x))) (identity x) (str "0" x) ))

(def today_ (str
(tt/year tt_date_2)
(pad_ (tt/month tt_date_2))
(pad_ (tt/day tt_date_2))
)
)

(defn convert_[date]
(str
(tt/year date)
(pad_ (tt/month date))
(pad_ (tt/day date))
))

;; does all logic of weekend adjusting
(def today_
(cond (pr/saturday? tt_date_) (identity  (str
(tt/year tt_date_1)
(pad_ (tt/month tt_date_1))
(pad_ (tt/day tt_date_1))
))
(pr/sunday? tt_date_) (identity  (str
(tt/year tt_date_2)
(pad_ (tt/month tt_date_2))
(pad_ (tt/day tt_date_2))
))
:else (convert_ tt_date_))
)

(def yesterday_
;; if saturday this is t-2
;; if sunday this t-3
;; else t-1
(cond (pr/saturday? tt_date_) (identity  (str
(tt/year tt_date_2)
(pad_ (tt/month tt_date_2))
(pad_ (tt/day tt_date_2))
))
(pr/sunday? tt_date_) (identity  (str
(tt/year tt_date_3)
(pad_ (tt/month tt_date_3))
(pad_ (tt/day tt_date_3))
))

(pr/monday? tt_date_) (identity  (str
(tt/year tt_date_3)
(pad_ (tt/month tt_date_3))
(pad_ (tt/day tt_date_3))
))
:else (convert_ tt_date_1)
))


;;;;;;; functions around date decision


;; find advs


(defn dailyBars
  [date1 date2 tic]
  (let [tail (str "&historyType=1&beginTime=" date1 "093000" "&endTime=" date2 "160000")]
(:body (client/get (str "http://localhost:5000/barData?symbol=" tic tail ))
)
    )
)

(def startDate (convert_ tt_date_14 ))
(identity startDate)

(def apple-data (clojure.string/split (dailyBars startDate today_ "AAPL") #"\r\n"))
(def N (count apple-data))
(identity N)


(defn adv [tic]
  (let [
        ;tic "AALO"
       apple-data (clojure.string/split (dailyBars startDate today_ tic) #"\r\n")
       N-local (count apple-data)
       volumes (map #(read-string (last (clojure.string/split % #","))) apple-data )
       close-price  (map #(read-string (second (clojure.string/split % #","))) apple-data )
       adv-in-millions (/ (stats/mean (map * volumes close-price)) 1E6)


       ] (if (< N-local N) (identity 0) (identity adv-in-millions)) ))

;;;find advs





(def advs (map adv (map first filter-data2-nasdaq)))
(count filter-data2-nasdaq)
(count advs)
(take 10 advs)
;; add advs to original dataset now tic name price mc and adv
(def filter-data3 (map vector (map first filter-data2-nasdaq)
                       (map second filter-data2-nasdaq)
                       (map #(nth % 2) filter-data2-nasdaq)
                       (map #(nth % 3) filter-data2-nasdaq)
                       advs
                       ))

(take 3 filter-data3 )
(def nasdaq-data (filter #(>= (nth % 4) adv-cutoff-millions) filter-data3 ))
(println (count nasdaq-data))
(def Ntake 20000)
(def nasdaq-data-small (take Ntake nasdaq-data))
;; nyse

(def advs (map adv (map first filter-data2-nyse)))



(def filter-data3 (map vector (map first filter-data2-nyse)
                       (map second filter-data2-nyse)
                       (map #(nth % 2) filter-data2-nyse)
                       (map #(nth % 3) filter-data2-nyse)
                       advs
                       ))
(def nyse-data (filter #(>= (nth % 4) adv-cutoff-millions) filter-data3 ))
(def nyse-data-small (take Ntake nyse-data))

(def all-eligible-tickers-with-repeats (concat nyse-data-small nasdaq-data-small ))

(comment
  (println (count all-eligible-tickers))
(println "set on tickers")
  (println (count (set (map first all-eligible-tickers))))
  )

 ;(def tickers (map first all-eligible-tickers))
;(def freqs (frequencies tickers ))
;(take 10 (reverse (sort-by second freqs)))
;(def repeat-tickers (set  (map first (filter #(< 1 (second %)) freqs))))
;(first repeat-tickers)

(def all-eligible-tickers (seq (set all-eligible-tickers-with-repeats  )))
(clojure.pprint/pprint all-eligible-tickers)

(:require [ clojure.pprint/pprint :as pprint] )
;;;; finished tracking all eligible tickers for trading, now see earnings page.

(def earnings-url "https://finance.yahoo.com/calendar/earnings?from=2017-05-14&to=2017-05-20&day=2017-05-12")

(def earnings-url "https://finance.yahoo.com/calendar/earnings?from=2017-05-07&to=2017-05-13&day=2017-05-09")



(defn get-html
  [index-url]
  (try (->
    index-url

    (client/get {:socket-timeout 10000 :conn-timeout 10000})
    (:body)
    )
       (catch Exception e (.getMessage e))
       (finally "Cant process index page")
       )

)

(defn get-index-titles-ahref
  [html_]
  (-> html_
    (enlive/html-snippet)
                                        ;   (enlive/select [:div.feature])
;       (enlive/select [:a])
       (enlive/select [:a] )
                                        ;(:attrs)
       ;(:data-symbol)
)
)


(defn get-index-titles-span
  [html_]
  (-> html_
    (enlive/html-snippet)
                                        ;   (enlive/select [:div.feature])
;       (enlive/select [:a])
       (enlive/select [:span] )
                                        ;(:attrs)
       ;(:data-symbol)
)
)






(pprint (map :content (get-index-titles-span (get-html earnings-url))))


(def earnings-symbols (remove nil? (map :data-symbol (map :attrs (get-index-titles-ahref (get-html earnings-url))))))

(count earnings-symbols)
(take 10 all-eligible-tickers)
(filter #(contains? (set (map first all-eligible-tickers )) % )earnings-symbols  )


(defn validtime? [x]
(contains? (set (concat '("Before Market Open")
               '("Time Not Supplied")
               '("After Market Close") ))x  )   )

(validtime? "Before Market Open")

(def strings  (filter string?  (flatten (map :content (get-index-titles-span (get-html earnings-url))))))
(pprint (take 5 strings))

(def search-pattern (re-pattern ".*EST.*|.*EST.*|.*After Market Close.*|.*Time Not Supplied .*|.*Before Market Open.*"))
(filter #(re-matches search-pattern %) strings )
(filter re-matches search-pattern ["ba" "After Market Close"] )


(pprint (take 5 (get-hours earnings-url)))









(defn get-hours
  [link]
  "returns a collection of possible titles"
  (let [html_
  (get-html link)
    structure (-> html_
    (enlive/html-snippet)
                  ;(enlive/select [  (enlive/attr-contains :style "center") :font  ]  )
                  )

    ;(enlive/select [[   (enlive/attr-contains :style "center") :font  ]])
    ;(enlive/select [  (enlive/attr-contains :style "center") :font  ]  )  ;; this says look for <div style="center"><font>STUFF YOU WANT </font> </div>
    possible1 (enlive/select structure [ [:td (enlive/attr-contains :class "data-col2 Ta(end) Pstart(15px) W(10%)")]   ]   )  ;; this says look for <div style="center"><font>STUFF YOU WANT </font> </div>


    structure-final (concat
                      ;(filter string? (flatten (map :content possible1)))
                      ;(filter string? (flatten (map :content possible2)))
                      ;(filter string? (flatten (map :content possible3)))
                      ;(filter string? (flatten (map :content possible4)))
                      possible1


)
;(enlive/select [:font]
    ;(enlive/select [ (enlive/attr-contains :style "center")  ])
    ;(fn[x](map :content x))
    ;first
    ;:content
    ;first
    ;:content
    ;firstkalo

     ]
    ;(identity possible5)
    (identity structure-final)
    ))
