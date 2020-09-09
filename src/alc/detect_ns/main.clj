;; POSSIBILITIES
;;
;; * try to find all ns and in-ns -- then use the most appropriate one
;;   based on current cursot position (this is likely to be hard?)

(ns alc.detect-ns.main
  (:require
   [alc.detect-ns.impl.ast :as ast]
   [alc.detect-ns.impl.ex :as ex]
   [alc.detect-ns.impl.validate :as validate]
   [clojure.java.io :as cji]
   [clojure.string :as cs])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn read-source
  [source]
  (if (string? source)
    (let [f (cji/file source)]
      (if-not (and (.exists f) (.isFile f))
        (ex/throw-info {:err-msg
                        (str "Argument not a readable file: " source)})
        (slurp source)))
    (slurp *in*)))

(defn main
  [& args]
  (let [source-str (read-source (first args))]
    (when-not (System/getenv "ALC_DETECT_NS_SKIP_VALIDATION")
      (validate/do-it source-str))
    (if-let [target-ns-name (ast/detect-ns source-str)]
      (println target-ns-name)
      (println))
    0))

(defn -main
  [& args]
  (let [status
        (try (apply main args)
             (catch Throwable t
               (binding [*out* *err*]
                 (if (= ex/msg-const (.getMessage t))
                   (do
                     (println (:err-msg (ex-data t)))
                     1)
                   (do
                     (println "Unexpected Throwable")
                     (.printStackTrace t)
                     2)))))]
    (flush)
    (System/exit status)))
