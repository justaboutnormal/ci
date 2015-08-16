(ns ci.util)

(defn reverse-ratings
  "Takes a map of maps and reverses it such that {:a {:1 'a1' :2 'a2'} :b {:1 'b1' :3 'b3'}}
  returns {:1 {:a 'a1' :b 'b1'} :2 {:a 'a2'} :3 {:b 'b3'}} "
  [source] (apply merge-with merge
                             (for [[ok ov] source
                                   [ik iv] ov]
                               {ik {ok iv}})))