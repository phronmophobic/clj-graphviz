# clj-graphviz

A wrapper of graphviz's underlying c libraries gvc and cgraph. 

A high level wrapper for rendering graphs can be found at `com.phronemophobic.clj-graphviz/render-graph`.

For direct access to gvc and cgraph, check out the `com.phronemophobic.clj-graphviz.raw`.

Built with [clong](https://github.com/phronmophobic/clong).

![clojure-cosmos](https://user-images.githubusercontent.com/156241/224561677-261b79db-bc20-4252-a872-332d6a54e67f.jpg)


## Documentation

[Documentation](https://phronmophobic.github.io/clj-graphviz/)  
[API Reference](https://phronmophobic.github.io/clj-graphviz/reference/)

## Dependency

Leiningen dependency:

```clojure
[com.phronemophobic/clj-graphviz "0.6.4"]
```

deps.edn dependency:

```clojure
com.phronemophobic/clj-graphviz {:mvn/version "0.6.4"}
```

## libgraphviz

clj-graphviz needs libgraphviz to installed on the system. The native dependencies can either be installed using a package manager or by including the maven dependency for your platform and architecture. 

### Clojars
Including multiple platforms and architectures is fine, but it will increase the size of your dependencies

```clojure
com.phronemophobic.cljonda/graphviz-darwin-aarch64 {:mvn/version "2.50.0-0.9.5"}
com.phronemophobic.cljonda/graphviz-darwin-x86-64 {:mvn/version "2.50.0-0.9.5"}
com.phronemophobic.cljonda/graphviz-linux-x86-64 {:mvn/version "2.50.0-0.9.5"}
```

### OS package manager

Examples:

Linux
`sudo apt install libgraphviz-dev`

Mac
`sudo port install graphviz-devel`

If you install libgraphviz via macports, you may need to add the macports jvm library path via an alias:
```
  :macports {:jvm-opts ["-Djna.library.path=/opt/local/lib"]}
```


## Basic Usage

```clojure
(require '[com.phronemophobic.clj-graphviz :refer [render-graph]])

(render-graph {:edges [["a" "b"]]})
;; writes to graph.png

(render-graph {:edges [["a" "b"]]}
              {:filename "my-graph.png"})

```

See [documentation](https://phronmophobic.github.io/clj-graphviz/) for more info.

## Related

[dorothy](https://github.com/daveray/dorothy): Hiccup-style generation of Graphviz graphs in Clojure and ClojureScript.

Dorothy works by first generating dot DSL code and then shelling out to the `dot` command line tool. `clj-graphviz` works directly with graphviz's underlying c library. The hope is to provide tighter integration with the graphviz library and expose underlying features that are hard to access via the `dot` command line tool (like graph layout).

## Future Work

Graphviz offers many options to tweak how graphs are rendered. It also offers graph layout without rendering. Exposing more of graphviz's functionality with a friendly clojure interface is a natural next step. Until then, the full graphviz API is available in its form under `com.phronemophobic.clj-graphviz.raw`.

For more information on what graphviz could do, check out https://graphviz.org/pdf/libguide.pdf.

## License

Copyright © 2022 Adrian Smith

Distributed under the Eclipse Public License version 1.0.
