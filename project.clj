(defproject alc.detect-ns "0.0.2"
  :description "Detect Namespace"
  :url "https://github.com/sogaiu/alc.detect-ns"
  :source-paths ["src"]
  :dependencies [[carocad/parcera "0.11.4"]
                 [clj-kondo/clj-kondo "2020.09.09"]
                 [org.antlr/antlr4-runtime "4.7.1"]
                 ;; parcera appears to need >= 1.10.x
                 [org.clojure/clojure "1.10.1"]]
  :profiles {:socket-repl
             {:jvm-opts
              ["-Dclojure.server.repl={:port 8379
                                       :accept clojure.core.server/repl}"]}
             ;; see script/compile
             :clojure-1.10.2-alpha1
             {:dependencies [[org.clojure/clojure "1.10.2-alpha1"]]}
             ;; see script/compile
             :native-image
             {:dependencies
              [[borkdude/clj-reflector-graal-java11-fix "0.0.1-graalvm-20.2.0"]
               [borkdude/sci.impl.reflector "0.0.1-java11"]]}
             ;;
             :uberjar {;;:global-vars {*assert* false}
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.skip-macros=true"]
                       :aot :all
                       :main alc.detect-ns.main}}
  :aliases {"alc.detect-ns" ["run" "-m" "alc.detect-ns.main"]})
