(ns unit.ibcm.core
  (:require [midje.sweet :refer :all]
            [ibcm.core :refer :all]))

(fact (count hex-digits) => (count ops))

(fact (:halt opmap) => \0)
(fact (:sub opmap)  => \6)
(fact (:brl opmap)  => \F)

(fact (label-map (clojure.string/split-lines (slurp "resources/test2.sibcm"))) => {})

(fact (encode-instruction {:type :basic :op :halt} {}) => "0000")
(fact (encode-instruction {:type :shift :op :shift :dir :right} {}) => "2400")
(fact (encode-instruction {:type :io :op :io :dir :in :format :hex} {})   => "1000")
(fact (encode-instruction {:type :io :op :io :dir :in :format :ascii} {}) => "1400")
(fact (encode-instruction {:type :io :op :io :dir :out :format :hex} {})  => "1800")
(fact (encode-instruction {:type :io :op :io :dir :out :format :ascii} {}) => "1C00")
(fact (encode-instruction {:type :addressed :op :add :address 100} {})    => "5100")
(fact (encode-instruction {:type :addressed :op :sub :address 450} {})    => "6450")

(fact (encode ".start" 0 {}) => "0000")
(fact (encode "jmp 000" 0 {})=>  "C000")
(fact (encode "halt" 0 {})   =>  "0000")
(fact (encode "add 100" 0 {})=>  "5100")
(fact (encode "readA" 0 {})  =>  "1400")
(fact (encode "readH" 0 {})  =>  "1000")
(fact (encode "printA" 0 {}) =>  "1C00")
(fact (encode "printH" 0 {}) =>  "1800")
(fact (encode "rotR 4" 0 {}) =>  "2C04")

(fact "CA" => (encode "jmp A" 0 {}))
(fact "B000" => (encode "nop" 0 {}))

(fact "001" => (get-address "001" {"a" "001" "start" "0aa"}))
(fact "0aa" => (get-address "start" {"a" "001" "start" "0aa"}))

(fact "halt\t" => (pad "halt"))
(fact "add 100" => (pad "add 100"))

(fact (-> "resources/test0.sibcm" slurp assemble :assembled) => "5100\t000\tadd 100\n0000\t001\thalt\t")
