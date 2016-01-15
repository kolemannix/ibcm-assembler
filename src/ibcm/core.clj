(ns ibcm.core
  (:require [ibcm.parse :as parse])
  (:gen-class))

;; Go easy on me - this was the first clojure code I ever wrote

(def hex-digits [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \A \B \C \D \E \F])
(def ops [:halt :io :shift :load :store :add :sub :and :or :xor :not :nop :jmp :jmpe :jmpl :brl])
(def opmap (zipmap ops hex-digits))

(def addresses (map #(format "%03x" %) (range 1000)))

(defn get-address [addr labelmap]
  (if (contains? labelmap addr)
    (labelmap addr) addr))

;; TODO finish label refactor
(defn encode-instruction [{instr-type :type :as instr} labels]
  "Takes a map created by parse-instr and returns an encoded ibcm instruction"
  (case instr-type
    :basic (let [opcode (:op instr)
                 opstr (opmap opcode)
                 data (or (:data instr) "000")]
             (str opstr data))
    :io (let [io-dir (:dir instr)
              io-format (:format instr)] 
          (cond 
            (= io-dir :out)
            (if (= io-format :hex) "1800" "1C00")
            (= io-dir :in)
            (if (= io-format :hex) "1000" "1400")))
    :shift (let [shift-dir (:dir instr)
                 rotate? (= :rot (:op instr))
                 count-char (or (:count instr) 0)] 
             (str (cond
                (= shift-dir :left)
                (if rotate? "280" "200")
                (= shift-dir :right)
                (if rotate? "2C0" "240")) count-char))
    :addressed (let [address (instr :address)
                     opcode (:op instr)]
                 (str (opmap opcode) (get-address address labels)))
    :else nil))
;; TODO shift count not included

(defn encode 
  [instr line labelmap]
  (if-let [parsed (try (parse/parse instr)
                                   (catch Exception e nil))]
    (try (encode-instruction parsed labelmap)
         (catch Exception e
           (throw (ex-info "Assembler Error"
                           {:line line
                            :instr instr}))))))

(defn pad [instr]
  (if (> (count instr) 5) instr (str instr \tab)))

(defn format-instruction [labelmap instr line comm]
  (str (encode instr line labelmap) \tab line \tab (pad instr) comm))

(defn label-map [parsed]
  (into {} (for [[instr line] (map list parsed addresses)
                 :when (:label instr)]
             [(:label instr) line])))

(defn assemble [source]
  (let [split-lines (map #(clojure.string/split % #";") (clojure.string/split-lines source))
        instrs (map first split-lines)
        parsed (map parse/parse instrs)
        labels (label-map parsed) 
        comments (for [comment (map second split-lines)]
                   (if comment (str "\t" ";" comment) ""))
        assembled (->> (map (partial format-instruction labels) instrs addresses comments)
                       (interpose "\n" ,,)
                       (clojure.string/join ,,))]
    {:source source :assembled assembled}))

(defn -main [& args]
  (spit (first args) (assemble (slurp (second args)))))

;; (assemble (slurp "resources/labels.sibcm"))
