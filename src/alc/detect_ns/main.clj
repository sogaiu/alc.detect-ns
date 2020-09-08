;; POSSIBILITIES
;;
;; * try to find all ns and in-ns -- then use the most appropriate one
;;   based on current cursot position (this is likely to be hard?)

(ns alc.detect-ns.main
  (:require
   [alc.detect-ns.impl.ast :as ast]
   [alc.detect-ns.impl.validate :as validate]
   [clojure.java.io :as cji]
   [clojure.string :as cs])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn main
  [& args]
  (let [first-arg (first args)
        slurped
        (if (string? first-arg)
          (let [f (cji/file first-arg)]
            (if-not (and (.exists f) (.isFile f))
              (throw (ex-info "ALC_DETECT_NS_THROW"
                              {:err-msg (str "Argument not a readable file: "
                                             first-arg)}))
              (slurp first-arg)))
          (slurp *in*))]
    (when-not (System/getenv "ALC_NS_DETECT_SKIP_VALIDATION")
      (when-let [findings (validate/check-source slurped)]
        (throw (ex-info
                "ALC_DETECT_NS_THROW"
                {:err-msg (str "Errors detected in source:\n"
                               (cs/join "\n"
                                        (map (fn [{:keys [message row]}]
                                               (str "  row:" row " - "
                                                    message))
                                             findings)))}))))
    (if-let [target-name (ast/detect-ns slurped)]
      (println target-name)
      (println))
    0))

(defn -main
  [& args]
  (let [exit
        (try (apply main args)
             (catch Exception e
               (if (= "ALC_DETECT_NS_THROW" (.getMessage e))
                 (let [{:keys [:err-msg]} (ex-data e)]
                   (binding [*out* *err*]
                     (println err-msg))
                   1)
                 (throw e)))
             (catch Throwable t
               (binding [*out* *err*]
                 (println "Unexpected Throwable"))
               (.printStackTrace t)
               2))]
    (flush)
    (System/exit exit)))
