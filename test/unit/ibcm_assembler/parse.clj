(ns unit.ibcm-assembler.parse
  (:require [ibcm-assembler.parse :refer :all]
            [midje.sweet :refer :all]))

(fact "Can parse io instructions properly"
      (parse-instruction "readH") => {:type :io :op :io :dir :in :format :hex}
      (parse-instruction "readA") => {:type :io :op :io :dir :in :format :ascii}
      (parse-instruction "printH") => {:type :io :op :io :dir :out :format :hex}
      (parse-instruction "printA") => {:type :io :op :io :dir :out :format :ascii})

(fact "Can parse label instructions properly"
      (parse-instruction ".a") => {:type :label :op :nop}
      (parse-instruction ".-_-.") => {:type :label :op :nop})

(fact "Can parse shift instructions properly"
      (parse-instruction "shiftL 1") => {:type :shift :op :shift :dir :left :count "1"}
      (parse-instruction "shiftR 15") => {:type :shift :op :shift :dir :right :count "15"}
      (parse-instruction "rotL 7") => {:type :shift :op :rot :dir :left :count "7"}
      (parse-instruction "rotR 1") => {:type :shift :op :rot :dir :right :count "1"})

(fact "Can parse addressed instructions properly"
      (parse-instruction "add 0001") => {:type :addressed :op :add :address "0001"})
