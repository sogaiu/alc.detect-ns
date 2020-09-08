# Limitations

* The content of the source file may make a difference.  The specific
  cases that may work include syntactically correct source files
  with the following limits on content:

  * Exactly 1 `ns` form at the top-level.

  * Exactly 1 `in-ns` form at the top-level, and no `ns` forms.

  My suspicion is that most Clojure source files meet these conditions, but
  this is just my impression.

* clj-kondo doesn't currently validate all valid Clojure source files
  (e.g. clojure.core is reported as having unresolved symbols).

  There are at least two work-arounds.

  1) Configure clj-kondo to not treat as errors certain things -- this will
     depend on specific details of the file in question.  See
     clj-kondo's documentation for details.

  2) Set the environment variable `ALC_DETECT_NS_SKIP_VALIDATION` to
     some value like `1` or `true`.  `alc.detect-ns` should then not
     use clj-kondo to validate source files.  The downside of this is
     that parsing may fail.
