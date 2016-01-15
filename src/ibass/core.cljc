(ns ibass.core
  (:require [ibass.assemble :as assemble :refer [assemble]]))

#?(:clj (defn -main [& args]
           (spit (first args) (assemble (slurp (second args))))))

#?(:cljs (.log js/console "Hello CLJS"))

;; (.log js/console "Goodbye CLJS")
