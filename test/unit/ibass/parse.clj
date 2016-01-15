(ns unit.ibass.parse
  (:require [ibass.parse :refer :all]
            [midje.sweet :refer :all]))

(fact (parse "readA") => {:type :io :op :io :dir :in :format :ascii})
(fact (parse "rotR 4") => {:type :shift :op :rot :dir :right :count "4"} )
(fact (parse "shiftL 100") => {:type :shift :op :shift :dir :left :count "100"})
(fact (parse "") => {:type :basic :op :nop})

(fact "Can parse io instructions properly"
      (parse "readH") => {:type :io :op :io :dir :in :format :hex}
      (parse "readA") => {:type :io :op :io :dir :in :format :ascii}
      (parse "printH") => {:type :io :op :io :dir :out :format :hex}
      (parse "printA") => {:type :io :op :io :dir :out :format :ascii})

(fact "Can parse label instructions properly"
      (parse ".a") => {:type :basic :op :halt :label "a"}
      (parse ".-_-.") => {:type :basic :op :halt :label "-_-."})

(fact "Can parse shift instructions properly"
      (parse "shiftL 1") => {:type :shift :op :shift :dir :left :count "1"}
      (parse "shiftR 15") => {:type :shift :op :shift :dir :right :count "15"}
      (parse "rotL 7") => {:type :shift :op :rot :dir :left :count "7"}
      (parse "rotR 1") => {:type :shift :op :rot :dir :right :count "1"})

(fact "Can parse addressed instructions properly"
      (parse "add 001") => {:type :addressed :op :add :address "001"})


