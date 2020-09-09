(ns alc.detect-ns.impl.ex)

(def msg-const
  "ALC_DETECT_NS_THROW")

(defn throw-info
  [m]
  (throw
   (ex-info msg-const m)))
