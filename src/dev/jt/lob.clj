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
