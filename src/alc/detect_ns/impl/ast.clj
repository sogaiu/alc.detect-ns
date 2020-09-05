(ns alc.detect-ns.impl.ast
  (:require
   [clojure.string :as cs]
   [parcera.core :as pc]))

(set! *warn-on-reflection* true)

(defn forms
  [src]
  (some->> (pc/ast src)
           rest))

(comment

  (def src
    (cs/join "\n"
             [";; another form"
              "(+ 2 2)"]))

  (forms src)
  #_ '((:comment ";; another form")
       (:whitespace "\n")
       (:list
        (:symbol "+")
        (:whitespace " ")
        (:number "2")
        (:whitespace " ")
        (:number "2")))

  (forms "")
  ;; => '()

  )

(defn first-form
  [src]
  (first (forms src)))

(comment

  (def some-src
    (cs/join "\n"
             ["(def a 1)"
              ""
              ":b"]))

  (pc/ast some-src)
  #_ '(:code
       (:list
        (:symbol "def") (:whitespace " ")
        (:symbol "a") (:whitespace " ")
        (:number "1"))
       (:whitespace "\n\n")
       (:keyword ":b"))

  (first-form some-src)
  #_ '(:list
       (:symbol "def") (:whitespace " ")
       (:symbol "a") (:whitespace " ")
       (:number "1"))

  (def src-with-line-comment
    (cs/join "\n"
             [";; hi there"
              ""
              "(+ 1 1)"]))

  (first-form src-with-line-comment)
  ;; => [:comment ";; hi there"]

  )

(defn list-node?
  [ast]
  (some-> (first ast)
          (= :list)))

(comment

  (list-node? (first-form "(+ 1 1)"))
  ;; => true

  )

(defn whitespace?
  [ast]
  (some-> (first ast)
          (= :whitespace)))

(comment

  (whitespace? '(:whitespace "\n  "))
  ;; => true

)

(defn line-comment?
  [ast]
  (some-> (first ast)
          (= :comment)))

(comment

  (line-comment? '(:comment ";; hi there"))
  ;; => true

  )

(defn comment-symbol?
  [ast]
  (when-let [head-elt (first ast)]
    (when (= head-elt :symbol)
      (when-let [next-elt (second ast)]
        (= next-elt "comment")))))

(comment

  (comment-symbol? '(:symbol "comment"))
  ;; => true

  )

(defn comment-block?
  [ast]
  (when-let [head-elt (first ast)]
    (when (= head-elt :list)
      (comment-symbol? (second ast)))))

(comment

  (def a-comment-block
    '(:list (:symbol "comment")))

  (comment-block? a-comment-block)
  ;; => true

  (comment-block? '(:comment ";; => 2"))
  ;; => nil

  (def src-with-comment-and-def
    (cs/join "\n"
             [""
              "(comment"
              ""
              "  (def b 2)"
              ""
              ")"
              ""
              "(def x 1)"]))

  (->> (pc/ast src-with-comment-and-def)
       rest
       (filter #(comment-block? %))
       count)
  ;; => 1

  )


(defn discard-with-form?
  [ast]
  (some-> (first ast)
          (= :discard)))

(comment

  (def src-with-discard
    "#_ {:a 1}")

  (pc/ast src-with-discard)
  #_ '(:code
       (:discard
        (:whitespace " ")
        (:map
         (:keyword ":a") (:whitespace " ")
         (:number "1"))))

  (discard-with-form? (first-form src-with-discard))
  ;; => true

  (discard-with-form?
   '(:discard
     (:whitespace " ")
     (:map
      (:keyword ":a") (:whitespace " ")
      (:number "1"))))
  ;; => true

  )

;; XXX: determine what else needs to be ignored
(defn list-head
  [ast]
  (assert (list-node? ast) (str "not a list: " ast))
  (->> (rest ast)
       (drop-while (fn [node]
                     ;; XXX: other things to filter out?
                     (or (whitespace? node)
                         (line-comment? node)
                         (comment-block? node)
                         (discard-with-form? node))))
       first))

(comment

  (first-form "(+ 1 1)")
  #_ '(:list
       (:symbol "+") (:whitespace " ")
       (:number "1") (:whitespace " ")
       (:number "1"))

  (list-head (first-form "(+ 1 1)"))
  ;; => '(:symbol "+")

  (list-head (first-form "( + 1 1)"))
  ;; => '(:symbol "+")

  (list-head (first-form (cs/join "\n"
                                  ["(;; hi"
                                   "+ 1 1)"])))
  ;; => '(:symbol "+")

  (list-head (first-form (cs/join "\n"
                                  ["((comment :a)\n"
                                   "+ 1 1)"])))
  ;; => '(:symbol "+")

  (list-head (first-form "(#_ - + 1 1)"))
  ;; => '(:symbol "+")

  )

(defn symbol-node?
  [ast]
  (some-> (first ast)
          (= :symbol)))

(comment

  (symbol-node? (first-form "hi"))
  ;; => true

  (symbol-node? (first-form ":hi"))
  ;; => false

  )

(defn symbol-name
  [ast]
  (assert (symbol-node? ast) (str "not symbol node: " ast))
  (second ast))

(comment

  (symbol-name (first-form "hi"))
  ;; => "hi"

  )

(defn ns-form-2?
  [ast]
  (when (and (list-node? ast)
             (symbol-node? (list-head ast))
             (= "ns" (symbol-name (list-head ast))))
    ast))

(comment

  (def src-with-just-ns
    "(ns fun-namespace.main)")

  (first-form src-with-just-ns)
  #_ '(:list
        (:symbol "ns") (:whitespace " ")
        (:symbol "fun-namespace.main"))

  (ns-form-2? (first-form src-with-just-ns))
  #_ '(:list
        (:symbol "ns") (:whitespace " ")
        (:symbol "fun-namespace.main"))

  (def src-with-ns
    ";; hi
(ns my-ns.core)

(defn a [] 1)

(def b 2)
")

  (some ns-form-2? (forms src-with-ns))
  #_ '(:list
       (:symbol "ns") (:whitespace " ")
       (:symbol "my-ns.core"))

  )

(defn in-ns-form-2?
  [ast]
  (when (and (list-node? ast)
             (symbol-node? (list-head ast))
             (= "in-ns" (symbol-name (list-head ast))))
    ast))

(comment

  (def src-with-in-ns
    "(in-ns 'clojure.core)")

  (first-form src-with-in-ns)
  #_ '(:list
       (:symbol "in-ns") (:whitespace " ")
       (:quote
        (:symbol "clojure.core")))

  (in-ns-form-2? (first-form src-with-in-ns))

  )

(defn metadata-node?
  [ast]
  (some-> (first ast)
          (= :metadata)))

(comment

  (metadata-node? (first-form "^:a [:x]"))
  ;; => true

  (metadata-node? (first-form "^:a ^:b {:x 2}"))
  ;; => true

  (metadata-node? (first-form ":a"))
  ;; => false

  )

(defn metadata-entry-node?
  [ast]
  (some-> (first ast)
          (= :metadata_entry)))

(comment

  (metadata-entry-node? (second (first-form "^:a [:x]")))
  ;; => true

  (metadata-entry-node? (second (first-form "^:a ^:b {:x 2}")))
  ;; => true

  (metadata-entry-node? (first-form ":a"))
  ;; => false

  )

;; XXX: likely not perfect
(defn metadatee
  [ast]
  (when (metadata-node? ast)
    (->> ast
         (drop-while (fn [node]
                       ;; XXX: probably missed some things
                       (or (not (coll? node))
                           (whitespace? node)
                           (line-comment? node)
                           (discard-with-form? node)
                           (metadata-entry-node? node))))
         first)))

(comment

  (metadatee (first-form "^:a [:x]"))
  #_ '(:vector
       (:keyword ":x"))

  (metadatee (first-form "^:a ^{:b 2} [:y]"))
  #_ '(:vector
       (:keyword ":y"))

  )

(defn name-of-ns
  [ns-ast]
  (when (ns-form-2? ns-ast)
    (->> ns-ast
         (keep (fn [node]
                 (when (coll? node)
                   (cond
                     (= (first node) :symbol)
                     , (second node)
                     (= (first node) :metadata)
                     , (second (metadatee node))
                     :else
                     nil))))
         second)))

(comment

  (def some-src-with-ns
    ";; hi

\"random string\"

(ns your-ns.core)

(defn x [] 8)

(def c [])
")

  (name-of-ns (some ns-form-2? (forms some-src-with-ns)))
  ;; => "your-ns.core"

  (def ns-with-meta
  "(ns ^{:doc \"some doc string\"
       :author \"some author\"}
  tricky-ns.here")

  (name-of-ns (some ns-form-2? (forms ns-with-meta)))
  ;; => "tricky-ns.here"

  )

(defn detect-ns
  [source]
  (->> (forms source)
       (some ns-form-2?)
       name-of-ns))

(comment

  (def sample-src-with-ns
    ";; nice comment
;; another nice comment

#_ putting-a-symbol-here-should-be-fin

(ns target-ns.main)

(comment

  ;; hey mate

)

(defn repl
  []
  :fun)

")

  (detect-ns sample-src-with-ns)
  ;; => "target-ns.main"

  (def src-with-ns-in-meta-node
  "(ns ^{:doc \"some doc string\"
       :author \"some author\"}
  funname.here
  (:refer-clojure :exclude (replace remove next)))")

  (forms src-with-ns-in-meta-node)
  #_ '((:list
        (:symbol "ns") (:whitespace " ")
        (:metadata
         (:metadata_entry
          (:map
           (:keyword ":doc") (:whitespace " ")
           (:string "\"some doc string\"") (:whitespace "\n       ")
           (:keyword ":author") (:whitespace " ")
           (:string "\"some author\"")))
         (:whitespace "\n  ")
         (:symbol "funname.here"))
        (:whitespace "\n  ")
        (:list
         (:keyword ":refer-clojure") (:whitespace " ")
         (:keyword ":exclude") (:whitespace " ")
         (:list
          (:symbol "replace") (:whitespace " ")
          (:symbol "remove") (:whitespace " ")
          (:symbol "next")))))

  (detect-ns src-with-ns-in-meta-node)
  ;; => "funname.here"

  )
