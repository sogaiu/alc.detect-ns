# Technical Details

All of the heavy-lifting is done by
[parcera](https://github.com/carocad/parcera).

The parcera library is used to parse a string of Clojure source code
to produce an AST.  The resulting AST is traversed searching for
top-level `ns` or `in-ns` forms.  Roughly, if either is found, an
attempt is made to extact a suitable name for the namespace in
question.
