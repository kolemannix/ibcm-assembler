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

(expect "0000" (encode-instruction {:instr-type :h :opcode :halt}))
(expect "2C00" (encode-instruction {:instr-type :s :opcode :shift :rotate? true :shift-dir :right}))
(expect "1000" (encode-instruction {:instr-type :io :opcode :io :io-dir :read :io-format :hex}))
(expect "1400" (encode-instruction {:instr-type :io :opcode :io :io-dir :read :io-format :ascii}))
(expect "1800" (encode-instruction {:instr-type :io :opcode :io :io-dir :write :io-format :hex}))
(expect "1C00" (encode-instruction {:instr-type :io :opcode :io :io-dir :write :io-format :ascii}))
(expect "5100" (encode-instruction {:instr-type :a :opcode :add :address 100}))
(expect "6450" (encode-instruction {:instr-type :a :opcode :sub :address 450}))

(expect {:instr-type :s :opcode :shift :rotate? true :shift-dir :right :shift-count "4"} (parse-instr "rotR 4"))
(expect {:instr-type :s :opcode :shift :rotate? false :shift-dir :left :shift-count "100"} (parse-instr "shiftL 100"))

(expect {:instr-type :io :opcode :io :io-dir :read :io-format :ascii} (parse-instr "readC"))

(expect "0000" (encode "halt"))
(expect "5100" (encode "add 100"))
(expect "1400" (encode "readC"))
(expect "1000" (encode "readH"))
(expect "1800" (encode "writeH"))
(expect "1C00" (encode "writeC"))

(expect "CA" (encode "jmp A"))
(expect "0003" (encode "dw 0003"))
(expect "B000" (encode "nop"))

(expect "halt\t" (pad "halt"))
(expect "add 100" (pad "add 100"))

(expect (slurp "resources/test1.ibcm") (assemble "resources/test1.sibcm"))
;(expect "C00A\t000\n0003\t001\nB000\t002\nB000\t003\nB000\t004\nB000\t005\nB000\t006\nB000\t007\nB000\t008\nB000\t009\n3001\t00a\n1800\t00b" (assemble "resources/test2.sibcm"))

(expect "5100\t000\tadd 100\n0000\t001\thalt\t" (assemble "resources/test0.sibcm"))
