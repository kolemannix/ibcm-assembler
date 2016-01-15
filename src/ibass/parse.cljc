(ns ibass.parse
  "Covers the 6 \"syntactic types\" of instructions in IBCM:
              :basic - Instructions with no arguments
              :label - Labels
              :io - IO Instructions
              :shift - Shift Instructions
              :addressed - Addressed Instructions (ones that take a single address as argument)"
  (:require [clojure.string :as string]))

(def instruction-types #{:basic :label :io :shift :addressed})

(let [address-instructions #{"load" "store" "add" "sub" "and" "or" "xor" "jmp" "jmpe" "jmpl" "brl"}]
  (defn- address-instruction? [op]
    (contains? address-instructions op)))

(defn- label-instruction? [op]
  (.startsWith op "."))

(def shift-instructions #{"shiftL" "shiftR" "rotL" "rotR"})

(defn- data-instruction? [op]
  (= (string/lower-case op) "dw"))

(def ^:private generic-instr
  {"" {:type :basic :op :nop}
   "nop" {:type :basic :op :nop}
   "halt" {:type :basic :op :halt}
   "readH" {:type :io :op :io :dir :in :format :hex}
   "readA" {:type :io :op :io :dir :in :format :ascii}
   "printH" {:type :io :op :io :dir :out :format :hex}
   "printA" {:type :io :op :io :dir :out :format :ascii}
   "not" {:type :basic :op :not}})

(defn parse-simple [op arg]
  (cond 
    (address-instruction? op) {:type :addressed :op (keyword op) :address arg}
    (contains? shift-instructions op) {:type :shift
                                       :op (if (.startsWith op "shift") :shift :rot)
                                       :dir (if (.endsWith op "L") :left :right)
                                       :count arg}
    (= (string/lower-case op) "dw") {:type :basic :op :halt :data arg} ;; DW's are technically HALTs
    :else (generic-instr op)))

(defn label [s]
  (subs s 1))

;; TODO validate number of digits

(defn parse [instruction]
  "Turn an instruction into data"
  (let [instr (string/split instruction #"\s+")]
    (if (.startsWith (first instr) ".")
      (if (= (count instr) 3)
        (assoc (parse-simple
                (second instr)
                (nth instr 2)) :label (label (first instr)))
        {:type :basic :op :halt :label (label (first instr))})
      (parse-simple (first instr) (second instr)))))
