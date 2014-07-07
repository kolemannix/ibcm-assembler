(ns ibcm-assembler.parse)

(def instruction-types #{:basic :label :io :shift :addressed})

(def blah  "Covers the 6 \"syntactic types\" of instructions in IBCM:
              :basic - Instructions with no arguments
              :label - Labels
              :io - IO Instructions
              :shift - Shift Instructions
              :addressed - Addressed Instructions (ones that take a single address as argument)")

(def ^:private parse-map
  {"" {:type :basic :op :nop}
   "nop" {:type :basic :op :nop}
   "halt" {:type :basic :op :halt}
   "readH" {:type :io :op :io :dir :in :format :hex}
   "readA" {:type :io :op :io :dir :in :format :ascii}
   "printH" {:type :io :op :io :dir :out :format :hex}
   "printA" {:type :io :op :io :dir :out :format :ascii}
   "not" {:type :basic :op :not}})

(let [address-instructions #{"load" "store" "add" "sub" "and" "or" "xor" "jmp" "jmpe" "jmpl" "brl"}]
  (defn- address-instruction? [op]
    (contains? address-instructions op)))

(defn- label-instruction? [op]
  (.startsWith op "."))

(let [shift-instructions #{"shiftL" "shiftR" "rotL" "rotR"}]
  (defn- shift-instruction? [op]
    (contains? shift-instructions op)))

(defn- parse-shift-instruction [[op arg]]
  {:type :shift
   :op (if (.startsWith op "shift") :shift :rot)
   :dir (if (.endsWith op "L") :left :right)
   :count arg})

(defn parse-instruction [instruction]
  "Turn an instruction into data"
  (let [[op arg :as instr] (clojure.string/split instruction #"\s+")]
    (cond 
     (label-instruction? op) {:type :label :op :nop}
     (address-instruction? op) {:type :a :op (keyword op) :address arg}
     (shift-instruction? op) (parse-shift-instruction instr)
     :else (when-let [parsed (parse-map op)] parsed))))
