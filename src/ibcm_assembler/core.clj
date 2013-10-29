(ns ibcm-assembler.core
  (:gen-class))

(def hex-digits [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \A \B \C \D \E \F])
(def op-names [:halt :io :shift :load :store :add :sub :and :or :xor :not :nop :jmp :jmpe :jmpl :brl])
(def opmap (zipmap op-names hex-digits))
(def instruction-type #{:h :io :s :a :d :l})
(def addresses (map #(format "%03x" %) (range 0 1000)))

(defn parse-op 
  "takes a string representing an IBCM operation and returns the appropriate hex digit"
  [op]
  (opmap (keyword op)))

(defn remove-first [input] (apply str (rest input)))
(defn parse-instr [instr]
  "takes an instruction as a string and returns a map representing the instruction"
  (let [instr-seq (clojure.string/split instr #"\s+")
        opcode (first instr-seq)]
    (cond 
      (or (empty? opcode) (= opcode "nop"))
      {:instr-type :h :opcode :nop}
      (.startsWith opcode ".")
      {:instr-type :l}
      (= opcode "dw")
      {:instr-type :d :opcode :dw :data (nth instr-seq 1)}
      (= opcode "halt")
      {:instr-type :h :opcode :halt}
      (= opcode "not")
      {:instr-type :h :opcode :not}
      (.startsWith opcode "shift")
      {:instr-type :s :opcode :shift :rotate? false :shift-dir (if (= (nth opcode 5) \L) :left :right) :shift-count (nth instr-seq 1)}
      (.startsWith opcode "rot")
      {:instr-type :s :opcode :shift :rotate? true :shift-dir (if (= (nth opcode 3) \L) :left :right) :shift-count (nth instr-seq 1)}
      (.startsWith opcode "read")
      {:instr-type :io :opcode :io :io-dir :read :io-format (if (= (nth opcode 4) \H) :hex :ascii)}
      (.startsWith opcode "print")
      {:instr-type :io :opcode :io :io-dir :write :io-format (if (= (nth opcode 5) \H) :hex :ascii)}
      :else {:instr-type :a :opcode (keyword opcode) :address (nth instr-seq 1)})))

(defn get-address [addr labelmap]
  (if (contains? labelmap addr)
    (labelmap addr)
    addr))

(defn encode-instruction [instr labels]
  "takes a map created by parse-instr and returns an encoded ibcm instruction"
  (def instr-type (instr :instr-type))
  (cond
    (= :d instr-type)
    (instr :data)
    (= :l instr-type)
    (str (opmap :nop) "000")
    (= :h instr-type) 
    (let [opcode (instr :opcode)
          opstr (opmap opcode)]
      (str opstr "000"))
    (= :io instr-type)
    (let [io-dir (instr :io-dir)
          io-format (instr :io-format)] 
      (cond 
        (= io-dir :write)
        (if (= io-format :hex) "1800" "1C00")
        (= io-dir :read)
        (if (= io-format :hex) "1000" "1400")))
    (= :s instr-type)
    (let [shift-dir (instr :shift-dir)
          rotate? (instr :rotate?)] 
      (cond
        (= shift-dir :left)
        (if rotate? "2400" "2000")
        (= shift-dir :right)
        (if rotate? "2C00" "2800")))
    (= :a instr-type)
    (let [address (instr :address)
          opcode (instr :opcode)] (str (opmap opcode) (get-address address labels)))))

(defn encode 
  ([instr] 
   (encode instr {}))
  ([instr labelmap]
   (encode-instruction (parse-instr instr) labelmap)))

(defn pad [instr]
  (if (> (count instr) 5) instr (str instr \tab)))
(defn format-instruction [op locn comm labelmap]
  (str (encode op labelmap) \tab locn \tab (pad op) comm))

(defn make-pair [line locn] [line locn])
;; TODO refactor label-map because i know there's about 1,000 better ways to do this
(defn label-map [lines]
  (let [with-dots  (->> (map make-pair lines addresses)
                        (filter #(= (ffirst %) \.))
                        flatten
                        (apply hash-map))]
    (zipmap (map remove-first (keys with-dots)) (vals with-dots))))

;; TODO note, I think I could totally use the state monad here to handle labels
;; I'm realizing that once I introduce a label map to these functions, I have two
;; options: either add an argument, the label map, or pass in the state as a monad
(defn assemble [x]
  (let [lines (clojure.string/split-lines (slurp x))
        locns (map #(format "%03x" %) (range 0 1000))
        labels (repeat (count lines) (label-map lines)) 
        instrs (map first (map #(clojure.string/split % #";") lines))
        comments (map (fn [x] (if (= 1 (count x)) "" (str \tab \; (second x)))) (map #(clojure.string/split % #";") lines))]
    (apply str (interpose "\n" (map format-instruction instrs locns comments labels)))))

(defn -main [& args]
  (spit (first args) (assemble (second args))))
