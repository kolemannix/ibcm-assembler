(ns ibcm-assembler.core-test
  (:require [midje.sweet :refer :all]
            [ibcm-assembler.core :refer :all]))

(fact (count hex-digits) => 16)
(fact (count op-names) => 16)

(fact \0 => (:halt opmap))
(fact \6 => (:sub opmap))
(fact \F => (:brl opmap))

(fact \F => (parse-op "brl"))
(fact \3 => (parse-op "load"))
(fact \4 => (parse-op "store"))
(fact \A => (parse-op "not"))

(fact "B000" => (encode ".start"))
(fact "C000" => (encode "jmp 000"))
(fact {} => (label-map (clojure.string/split-lines (slurp "resources/test2.sibcm"))))

(fact "0000" => (encode-instruction {:instr-type :h :opcode :halt} {}))
(fact "2C00" => (encode-instruction {:instr-type :s :opcode :shift :rotate? true :shift-dir :right} {}))
(fact "1000" => (encode-instruction {:instr-type :io :opcode :io :io-dir :read :io-format :hex} {}))
(fact "1400" => (encode-instruction {:instr-type :io :opcode :io :io-dir :read :io-format :ascii} {}))
(fact "1800" => (encode-instruction {:instr-type :io :opcode :io :io-dir :write :io-format :hex} {}))
(fact "1C00" => (encode-instruction {:instr-type :io :opcode :io :io-dir :write :io-format :ascii} {}))
(fact "5100" => (encode-instruction {:instr-type :a :opcode :add :address 100} {}))
(fact "6450" => (encode-instruction {:instr-type :a :opcode :sub :address 450} {}))
(fact {:instr-type :s :opcode :shift :rotate? true :shift-dir :right :shift-count "4"} => (parse-instr "rotR 4"))
(fact {:instr-type :s :opcode :shift :rotate? false :shift-dir :left :shift-count "100"} => (parse-instr "shiftL 100"))

(fact {:instr-type :io :opcode :io :io-dir :read :io-format :ascii} => (parse-instr "readC"))

(fact "0000" => (encode "halt"))
(fact "5100" => (encode "add 100"))
(fact "1400" => (encode "readC"))
(fact "1000" => (encode "readH"))
(fact "1800" => (encode "printH"))
(fact "1C00" => (encode "printC"))

(fact "CA" => (encode "jmp A"))
(fact "0003" => (encode "dw 0003"))
(fact "B000" => (encode "nop"))

(fact "001" => (get-address "001" {"a" "001" "start" "0aa"}))
(fact "0aa" => (get-address "start" {"a" "001" "start" "0aa"}))

(fact "halt\t" => (pad "halt"))
(fact "add 100" => (pad "add 100"))

(fact "5100\t000\tadd 100\n0000\t001\thalt\t" => (assemble "resources/test0.sibcm"))
