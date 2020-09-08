;; POSSIBILITIES
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

(defn main
  [& args]
  (let [first-arg (first args)
        slurped
        (if (string? first-arg)
          (let [f (cji/file first-arg)]
            (if-not (and (.exists f) (.isFile f))
              (do
                (str "Argument not a readable file: " first-arg)
                1)
              (slurp first-arg)))
          (slurp *in*))]
    (when-not (System/getenv "ALC_NS_DETECT_SKIP_VALIDATION")
      (when-let [findings (validate/check-source slurped)]
        (binding [*out* *err*]
          (println "Errors detected in source")
          (doseq [{:keys [message row]} findings]
            (println "row:" row " - " message)))
        1))
    (if-let [target-name (ast/detect-ns slurped)]
      (println target-name)
      (println)))
  0)

(defn -main
  [& args]
  (let [exit
        (try (apply main args)
             (catch Throwable e
               (binding [*out* *err*]
                 (println "Unexpected Throwable"))
               (.printStackTrace e)))]
    (flush)
    (System/exit exit)))
