(ns dev.jt.lob
  (:require [clojure.data.priority-map :refer [priority-map-keyfn]]))

(defn empty-level []
  (with-meta (priority-map-keyfn first) {::total-vol 0}))

(defn level-size [px-level]
  (::total-vol (meta px-level)))

(defn empty-ask-side [] (sorted-map))
(defn empty-bid-side [] (sorted-map-by (comp - compare)))

(defn empty-lob []
  {::asks (empty-ask-side)
   ::bids (empty-bid-side)})

(defn insert [lob side px id time sz]
  (update-in lob [side px]
    (fn [level]
      (-> (or level (empty-level)) 
          (assoc id [time sz])
          (vary-meta update ::total-vol + sz)))))

(defn delete [lob side px id]
  (let [size   (get-in lob [side px id 1])
        lvl-sz (level-size (get-in lob [side px]))]
    (if (= size lvl-sz)
      (update lob side dissoc px)
      (cond-> lob
        size
        (update-in [side px]
          (fn [level]
            (-> level
                (dissoc id)
                (vary-meta update ::total-vol - size))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; experimental/wip

(defn apply-sides [lob f]
  (-> lob
      (update ::asks f)
      (update ::bids f)))


(comment
  (require '[dev.jt.lob :as lob])
  (def t1 (java.time.Instant/now))
  (def lob1 (-> (lob/empty-lob)
                (lob/insert ::lob/asks 100M  123 (.plusSeconds t1 1) 1.00)
                (lob/insert ::lob/asks 100M  222 (.plusSeconds t1 2) 4.00)
                (lob/insert ::lob/asks 100M  244 (.plusSeconds t1 3) 0.45)
                (lob/insert ::lob/asks 101M  123 (.plusSeconds t1 4) 1.00)
                (lob/insert ::lob/bids 99M   456 (.plusSeconds t1 5) 1.00)
                (lob/insert ::lob/bids 99M   789 (.plusSeconds t1 6) 1.02)
                (lob/delete ::lob/bids 99M   789)
                (lob/insert ::lob/bids 97M   999 (.plusSeconds t1 7) 5.0))))
