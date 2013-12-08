(ns ibcm-assembler.core-test
  (:require [expectations :refer :all]
            [ibcm-assembler.core :refer :all]))

(expect 16 (count hex-digits))
(expect 16 (count op-names))

(expect \0 (:halt opmap))
(expect \6 (:sub opmap))
(expect \F (:brl opmap))

(expect \F (parse-op "brl"))
(expect \3 (parse-op "load"))
(expect \4 (parse-op "store"))
(expect \A (parse-op "not"))

(expect "B000" (encode ".start"))
(expect "C000" (encode "jmp 000"))
(expect {} (label-map (clojure.string/split-lines (slurp "resources/test2.sibcm"))))

(expect "0000" (encode-instruction {:instr-type :h :opcode :halt} {}))
(expect "2C00" (encode-instruction {:instr-type :s :opcode :shift :rotate? true :shift-dir :right} {}))
(expect "1000" (encode-instruction {:instr-type :io :opcode :io :io-dir :read :io-format :hex} {}))
(expect "1400" (encode-instruction {:instr-type :io :opcode :io :io-dir :read :io-format :ascii} {}))
(expect "1800" (encode-instruction {:instr-type :io :opcode :io :io-dir :write :io-format :hex} {}))
(expect "1C00" (encode-instruction {:instr-type :io :opcode :io :io-dir :write :io-format :ascii} {}))
(expect "5100" (encode-instruction {:instr-type :a :opcode :add :address 100} {}))
(expect "6450" (encode-instruction {:instr-type :a :opcode :sub :address 450} {}))
(expect {:instr-type :s :opcode :shift :rotate? true :shift-dir :right :shift-count "4"} (parse-instr "rotR 4"))
(expect {:instr-type :s :opcode :shift :rotate? false :shift-dir :left :shift-count "100"} (parse-instr "shiftL 100"))

(expect {:instr-type :io :opcode :io :io-dir :read :io-format :ascii} (parse-instr "readC"))

(expect "0000" (encode "halt"))
(expect "5100" (encode "add 100"))
(expect "1400" (encode "readC"))
(expect "1000" (encode "readH"))
(expect "1800" (encode "printH"))
(expect "1C00" (encode "printC"))

(expect "CA" (encode "jmp A"))
(expect "0003" (encode "dw 0003"))
(expect "B000" (encode "nop"))

(expect "001" (get-address "001" {"a" "001" "start" "0aa"}))
(expect "0aa" (get-address "start" {"a" "001" "start" "0aa"}))

(expect "halt\t" (pad "halt"))
(expect "add 100" (pad "add 100"))

(expect "5100\t000\tadd 100\n0000\t001\thalt\t" (assemble "resources/test0.sibcm"))
