# clj-graphviz

A wrapper of graphviz's underlying c libraries gvc and cgraph. 

A high level wrapper for rendering graphs can be found at `com.phronemophobic.clj-graphviz/render-graph`.

For direct access to gvc and cgraph, check out the `com.phronemophobic.clj-graphviz.raw.gvc` and `com.phronemophobic.clj-graphviz.raw.cgraph` namespaces.

Built with [clong](https://github.com/phronmophobic/clong).

## Dependency

Leiningen dependency:

```clojure
[com.phronemophobic/clj-graphviz "0.1.0"]
```

deps.edn dependency:

```clojure
com.phronemophobic/clj-graphviz {:mvn/version "0.1.0"}
```

## libgraphviz

clj-graphviz needs libgraphviz to installed on the system. The easiest way to do that is typically through a package manager.

Examples:

Linux
`sudo apt install libgraphviz-dev`

Mac
`sudo port install graphviz-devel`

## Usage

```clojure
(require '[com.phronemophobic.clj-graphviz :refer [render-graph]])

(render-graph {:edge [["a" "b"]]})
;; writes to graph.png

(render-graph {:edge [["a" "b"]]}
              {:filename "my-graph.png"})

```

## Future Work

Graphviz offers many options to tweak how graphs are rendered. It also offers graph layout without rendering. Exposing more of graphviz's functionality with a friendly clojure interface is a natural next step. Until then, the full graphviz API is available in its form under `com.phronemophobic.clj-graphviz.raw.gvc` and `com.phronemophobic.clj-graphviz.raw.cgraph`.

For more information on what graphviz could do, check out https://graphviz.org/pdf/libguide.pdf.

## License

Copyright © 2022 Adrian Smith

Distributed under the Eclipse Public License version 1.0.
