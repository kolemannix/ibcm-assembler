(ns ibcm-assembler.core
  (:gen-class))

;; Go easy on me - this was the first clojure code I ever wrote

(def hex-digits [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \A \B \C \D \E \F])
(def op-names [:halt :io :shift :load :store :add :sub :and :or :xor :not :nop :jmp :jmpe :jmpl :brl])
(def opmap (zipmap op-names hex-digits))
(def instruction-type #{:h :io :s :a :d :l})
(def addresses (map #(format "%03x" %) (range 1000)))

(defn parse-op 
  "takes a string representing an IBCM operation and returns the appropriate hex digit"
  [op]
  (opmap (keyword op)))

(defn remove-first [input] (clojure.string/join (rest input)))

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
     :else
     {:instr-type :a :opcode (keyword opcode) :address (nth instr-seq 1)})))

(defn get-address [addr labelmap]
  (if (contains? labelmap addr)
    (labelmap addr)
    addr))

(defn encode-instruction [{instr-type :instr-type :as instr} labels]
  "Takes a map created by parse-instr and returns an encoded ibcm instruction"
  (case instr-type
    :d (instr :data)
    :l (str (opmap :nop) "000")
    :h (let [opcode (instr :opcode)
             opstr (opmap opcode)]
         (str opstr "000"))
    :io (let [io-dir (instr :io-dir)
              io-format (instr :io-format)] 
          (cond 
           (= io-dir :write)
           (if (= io-format :hex) "1800" "1C00")
           (= io-dir :read)
           (if (= io-format :hex) "1000" "1400")))
    :s (let [shift-dir (instr :shift-dir)
             rotate? (instr :rotate?)] 
         (cond
          (= shift-dir :left)
          (if rotate? "2400" "2000")
          (= shift-dir :right)
          (if rotate? "2C00" "2800")))
    :a (let [address (instr :address)
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

(defn label-map [lines]
  (into {} (->> (map list lines addresses)  
                (filter (fn [[k v]] (and
                                     (not (empty? k))
                                     (= (subs k 0 1) "."))))
                (map (fn [[k v]] [(subs k 1) v])))))

(defn assemble [x]
  (let [lines (clojure.string/split-lines (slurp x))
        labels (label-map lines) 
        format-fn (fn [a b c] (format-instruction a b c labels))
        split-lines (map #(clojure.string/split % #";") lines)
        instrs (map first split-lines)
        comments (for [comment (map second split-lines)]
                   (do (println comment) (if comment
                            (str "\t" ";" comment)
                            "")))]
    (clojure.string/join (interpose "\n" (map format-fn instrs addresses comments)))))

(defn -main [& args]
  (spit (first args) (assemble (second args))))
