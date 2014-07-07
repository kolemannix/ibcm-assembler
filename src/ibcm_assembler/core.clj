(ns ibcm-assembler.core
  (:require [ibcm-assembler.parse :as parse])
  (:gen-class))

;; Go easy on me - this was the first clojure code I ever wrote

(def hex-digits [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \A \B \C \D \E \F])
(def ops [:halt :io :shift :load :store :add :sub :and :or :xor :not :nop :jmp :jmpe :jmpl :brl])
(def opmap (zipmap ops hex-digits))

(def addresses (map #(format "%03x" %) (range 1000)))

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
  [instr locn labelmap]
  (if-let [parsed-instruction (try (parse-instruction instr)
                                   (catch Exception e nil))]
    (encode-instruction parsed-instruction labelmap)
    (throw (Exception. (format "Assembler Error on line %s, unrecognized instruction: %s" locn instr)))))

(defn pad [instr]
  (if (> (count instr) 5) instr (str instr \tab)))

(defn format-instruction [op locn comm labelmap]
  (str (encode op locn labelmap) \tab locn \tab (pad op) comm))

(defn label-map [lines]
  (into {} (->> (map list lines addresses)  
                (filter (fn [[k v]] (and
                                     (not (empty? k))
                                     (= (subs k 0 1) "."))))
                (map (fn [[k v]] [(subs k 1) v])))))

(defn assemble [source]
  (let [lines (clojure.string/split-lines source)
        labels (label-map lines) 
        format-fn (fn [a b c] (format-instruction a b c labels))
        split-lines (map #(clojure.string/split % #";") lines)
        instrs (map first split-lines)
        comments (for [comment (map second split-lines)]
                   (if comment (str "\t" ";" comment) ""))
        assembled (clojure.string/join (interpose "\n" (map format-fn instrs addresses comments)))]
    {:source source
     :assembled assembled}))

(defn -main [& args]
  (spit (first args) (assemble (slurp (second args)))))

(println (assemble (slurp "example/addition.sibcm")))
