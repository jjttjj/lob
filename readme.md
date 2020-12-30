# lob

A somewhat efficient limit order book in clojure. 
A `lob` is a map of bid/ask sides.
A `side` is a sorted map of `price` (a `bigdec`) to `level`, sorted by distance to the best bid/ask.
A `level` is a priority map of `id` (a unique value) to a vector of `time` and `size`, sorted by `time`.

# api

```

(empty-lob)
(insert [lob side px id time sz])
(delete [lob side px id])
```


```clojure
(require '[dev.jt.lob :as lob])
(def t1 (java.time.Instant/now))
(def lob1 (-> (lob/empty-lob)
              (lob/insert ::lob/asks 100M  123 (.plusSeconds t1 1) 1.00)
              (lob/insert ::lob/asks 100M  222 (.plusSeconds t1 2) 4.00)
              (lob/insert ::lob/asks 100M  244 (.plusSeconds t1 3) 0.45)
              (lob/insert ::lob/asks 101M  555 (.plusSeconds t1 4) 1.00)
              (lob/insert ::lob/bids 99M   456 (.plusSeconds t1 5) 1.00)
              (lob/insert ::lob/bids 99M   789 (.plusSeconds t1 6) 1.02)
              (lob/delete ::lob/bids 99M   789)
              (lob/insert ::lob/bids 97M   999 (.plusSeconds t1 7) 5.0)))
              
              
```
```clojure

lob1
;;; result
{:dev.jt.lob/asks
 ;;sorted map of prices -> price level, sorted by prices, ascending
 {100M

  ;; sorted map of order-id -> [time size] by first of the value vector (the time)
  {123 [#object[java.time.Instant ,,, "2020-12-30T21:02:37.222220600Z"] 1.0],
        222 [#object[java.time.Instant ,,, "2020-12-30T21:02:38.222220600Z"] 4.0],
        244 [#object[java.time.Instant ,,, "2020-12-30T21:02:39.222220600Z"] 0.45]},
  101M {123 [#object[java.time.Instant ,,, "2020-12-30T21:02:40.222220600Z"] 1.0]}}

 :dev.jt.lob/bids
 ;;sorted map of prices -> price level, sorted by prices, descending

 {99M
  ;; sorted map of order-id -> [time size] by first of the value vector (the time)
  {456 [#object[java.time.Instant ,,, "2020-12-30T21:02:41.222220600Z"] 1.0]},
  97M {999 [#object[java.time.Instant ,,, "2020-12-30T21:02:43.222220600Z"] 5.0]}}}
  
  
  
(-> lob1 ::lob/asks first val lob/level-size) ;;5.45
```

# Inspired by

[How To Build a Fast Limit Order Book](https://gist.github.com/halfelf/db1ae032dc34278968f8bf31ee999a25) (reproduced)
