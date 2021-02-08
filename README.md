# alc.detect-ns

## Purpose

Detect an appropriate namespace for a file or string of source code.

## Status

Early stage.

## Prerequisites

* Running: clj / tools.deps(.alpha) -- unless using native-image executable
* Building executable (optional): Leiningen + Graalvm + native-image

## Use Cases and Limitations

[When](doc/use-cases.md) this might be useful, and the [fine
print](doc/limitations.md).

## Quick Trial

Suppose there is a file with Clojure source with file path
`~/src/my-project/src/fun.clj`

Detect a namespace for the file by:

```
$ cat ~/src/my-project/src/fun.clj | clj -Sdeps '{:deps {alc.detect-ns {:git/url "https://github.com/sogaiu/alc.detect-ns" :sha "46bbe75977bbed25d3db026ab053816434e77615"}}}' -m alc.detect-ns.main
```

or:

```
$ clj -Sdeps '{:deps {alc.detect-ns {:git/url "https://github.com/sogaiu/alc.detect-ns" :sha "46bbe75977bbed25d3db026ab053816434e77615"}}}' -m alc.detect-ns.main ~/src/my-project/src/fun.clj
```

## General Setup and Use

alc.detect-ns can be used via `clj` via appropriate configuration of
`deps.edn`.  It can also be used via a native-image binary
`alc.detect-ns` (see below for building instructions).

To use via `clj`, first edit `~/.clojure/deps.edn` to have an alias
like:

```
...
:aliases
{
 :detect-ns ; or :alc.detect-ns
 {
  :extra-deps {sogaiu/alc.detect-ns
                {:git/url "https://github.com/sogaiu/alc.detect-ns"
                 :sha "46bbe75977bbed25d3db026ab053816434e77615"}}
  :main-opts ["-m" "alc.detect-ns.main"]
 }
```

To detect a namespace for a file (e.g. `src/fun.clj`):

```
$ cat src/fun.clj | clj -A:detect-ns
```

or with the native-image binary:

```
$ cat src/fun.clj | alc.detect-ns
```

## Building

Building the native-image binary requires Leiningen and Graalvm.

### Linux and macos

With Leiningen installed and Graalvm 20.2.0 for Java 11 uncompressed
at `$HOME/src/graalvm-ce-java11-20.2.0`:

```
git clone https://github.com/sogaiu/alc.detect-ns
cd alc.detect-ns
export GRAALVM_HOME=$HOME/src/graalvm-ce-java11-20.2.0
export PATH=$GRAALVM_HOME/bin:$PATH
bash script/compile
```

This should produce a binary named `alc.detect-ns`.  Putting this or a
symlink to it on `PATH` might make things more convenient.

### Windows 10

With Leiningen installed and Graalvm 20.2.0 for Java 11 uncompressed
at `C:\Users\user\Desktop\graalvm-ce-java11-20.2.0`, in a x64 Native
Tools Command Prompt:

```
git clone https://github.com/sogaiu/alc.detect-ns
cd alc.detect-ns
set GRAALVM_HOME=C:\Users\user\Desktop\graalvm-ce-java11-20.2.0
.\script\compile.bat
```

This should produce a binary named `alc.detect-ns.exe`.  Putting this on
`PATH` might make things more convenient.

Note that on Windows, one of the usage invocations might be like:

```
C:\Users\user\Desktop\alc.detect-ns> type src\alc\x_as_tests\impl\ast.clj | .\alc.detect-ns.exe
```

## Technical Details

Curious about some [technical details](doc/technical-details.md)?  No?
That's why they are not on this page :)

## Acknowledgments

* borkdude - babashka, clj-kondo, graalvm native-image work, pod-babashka-parcera, discussions, etc.
* carocad - parcera
* lread - discussions
