# clj-graphviz

A wrapper of graphviz's underlying c libraries gvc and cgraph. 

A high level wrapper for rendering graphs can be found at `com.phronemophobic.clj-graphviz/render-graph`.

For direct access to gvc and cgraph, check out the `com.phronemophobic.clj-graphviz.raw.gvc` and `com.phronemophobic.clj-graphviz.raw.cgraph` namespaces.

## Dependency

Leiningen dependency:

```clojure
[com.phronemophobic/clj-graphviz "0.1.0"]
```

deps.edn dependency:

```clojure
com.phronemophobic/clj-graphviz {:mvn/version "0.1.0"}
```

## Usage

```clojure
(require '[com.phronemophobic.clj-graphviz :refer [render-graph]])

(render-graph {:edge [["a" "b"]]})
;; writes to graph.png

(render-graph {:edge [["a" "b"]]}
              {:filename "my-graph.png"})

```
## License

Copyright © 2022 Adrian Smith

Distributed under the Eclipse Public License version 1.0.
