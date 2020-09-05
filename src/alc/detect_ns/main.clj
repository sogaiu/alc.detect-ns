;; POSSIBILITIES
;;
;; * mode to not validate -- e.g. to handle clojure.core
;;
;; * detect in-ns
;;
;; * try to find all ns and in-ns -- then use the most appropriate one
;;   based on current cursot position (this is likely to be hard?)

(ns alc.detect-ns.main
  (:require
   [alc.detect-ns.impl.ast :as ast]
   [alc.detect-ns.impl.validate :as validate]
   [clojure.java.io :as cji])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn -main
  [& args]
  (let [first-arg (first args)
        slurped
        (if (string? first-arg)
          (let [f (cji/file first-arg)]
            (assert (and (.exists f) (.isFile f))
                    (str "argument not a readable file: " first-arg))
            (slurp first-arg))
          (slurp *in*))]
    (when-let [findings (validate/check-source slurped)]
      (binding [*out* *err*]
        (println "Errors detected in source")
        (doseq [{:keys [message row]} findings]
          (println "row:" row " - " message)))
      (System/exit 1))
    (if-let [target-name (ast/detect-ns slurped)]
      (println target-name)
      (println)))
  (flush)
  (System/exit 0))
