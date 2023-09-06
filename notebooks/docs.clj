^{:nextjournal.clerk/visibility {:code :hide :result :hide}
  :nextjournal.clerk/toc true}
(ns docs
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as v]
            [com.phronemophobic.clj-graphviz :refer [render-graph]]
            [com.phronemophobic.clj-graphviz.spec :as gspec]
            [clojure.java.io :as io])
  (:import javax.imageio.ImageIO))

{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn transform-child-viewers [viewer update-fn & update-args]
  (update viewer :transform-fn (partial comp #(apply update % :nextjournal/viewers v/update-viewers update-args))))

(def table-viewer-no-pagination
  (transform-child-viewers v/table-viewer v/update-viewers {:page-size #(dissoc % :page-size)}))

(def graph-viewer
  {:pred ::graph
   :transform-fn
   (clerk/update-val
    
    (fn [m]
      (let [path (str "tmp/graph-" (hash m) ".png")]
        (.mkdirs (-> (io/file path)
                     (.getParentFile)))
        (if-let [opts (get m ::opts {})]
          (render-graph (::graph m)
                        (assoc opts
                               :filename path)))
        
        (clerk/col
         {::clerk/width :wide}
         (clerk/md
          (str "```clojure\n"

               (with-out-str
                 (clojure.pprint/pprint
                  (if-let [opts (get m ::opts)]
                    (list 'render-graph (::graph m) opts)
                    (list 'render-graph (::graph m)))))
               "\n```"))
         (ImageIO/read (io/file path))))))})

(clerk/add-viewers! [graph-viewer])

{:nextjournal.clerk/visibility {:code :hide :result :show}}

;; # clj-graphviz

;; [clj-graphviz](https://github.com/phronmophobic/clj-graphviz) is a clojure wrapper for the libgraphviz c libraries.

;; ## Dependency

;; Leiningen dependency:

;; ```clojure
;; [com.phronemophobic/clj-graphviz "0.6.2"]
;; ```

;; deps.edn dependency:

;; ```clojure
;; com.phronemophobic/clj-graphviz {:mvn/version "0.6.2"}
;; ```

;; ## Usage

;; The main entrypoint for `clj-graphviz`'s high level clojure api
;; is `render-graph`.

;; ```clojure
;; (require '[com.phronemophobic.clj-graphviz :refer [render-graph]])
;; ```

;; `render-graph` can be called like
;;```clojure
;;(render-graph graph)
;; ;; or
;;(render-graph graph opts)
;; ```

;; A simple example:

{::graph {:nodes ["a" "b" "c"]}}

;; ## Graphs

;; The first argument to `render-graph` is a graph.

{::graph {:edges [["a" "b"]]}}

;; A graph is map of the keys: `:nodes`, `:edges`, `:default-attributes` and `:flags`.
;; - `:nodes`: A sequence of nodes. Redundant if all nodes have edges.
;; - `:edges`: A sequences of edges.
;; - `:default-attributes`: A map of node types to their default attributes.
;; - `:flags`: flags should be a subset of `#{:directed :strict}`.

;; Each of these keys is described below.

;; ### Graph Nodes

;; A graph node is either a string node-id or map.
;; ```clojure
;; ;; string node id
;; "my-node-id"
;; ;; map, must have `:id`
;; {:id "my-node-id"}
;; ;; may also have other attributes
;; {:id "my-node-id", :color "red"}
;; ```


;; The simplest way to specify a graph is 
{::graph {:nodes ["a" "b" "c"]}}

;; Alternatively, nodes can be maps. The only required key is `:id`.
{::graph {:nodes [{:id "a"}
                  {:id "b"}
                  {:id "c"}]}}

;; Nodes can have additional keys with attributes.
{::graph {:nodes [{:id "red"
                   :color "red"}
                  {:id "green"
                   :color "green"}]}}

;; Attribute values are always strings, even if they represent numbers.
{::graph {:nodes [{:id "large"
                   :fontsize "50"}]}}

;; String nodes and map nodes can be mixed.
{::graph {:nodes [{:id "large"
                   :fontsize "50"}
                  "other"]}}

;; ### Graph Edges

;; A graph edge is either a tuple of 2 edges [from, to] or a map with the keys `:from`, `:to`, and any attributes.

;; ```clojure
;; ;; tuple of edges
;; ["from-id" "to-id"]
;; ;; map, must have `:from` and `:to` keys
;; {:from "from-id" :to "to-id"}
;; ;; may also have other attributes
;; {:from "from-id", :to "to-id" :color "red"}
;; ;; nodes can be either string ids or maps
;; {:from "from-id", :to {:id "to-id"}}
;; ```

;; #### Graph Edge examples

{::graph {:edges [["a" "b"]]}}

{::graph {:edges [{:from "a"
                   :to "b"}]}}

{::graph {:edges [{:from "a", :to "b" :color "red"}]}}

;; ## Graph Flags

;; Graph flags are a subset of `#{:strict :directed}`.

;; ### Strict Flag

;; A strict graph will have at most one edge between any two nodes.

{::graph {:edges [["a" "b"]
                  ["a" "b"]]
          :flags #{:strict}}}

;; A non strict graph can have multiple edges between two nodes.

{::graph {:edges [
                  {:from "a"
                   :to "b"
                   ;; :color "#FF000088"
                   ;; :penwidth "10.0"
                   }
                  {:from "a"
                   :to "b"
                   ;; :color "green"
                   ;; :penwidth "1.0"
                   }]
          }}

;; ### Directed Flag

;; A graph can be marked as directed by passing `:directed` as a flag
{::graph {:edges [["a" "b"]]
          :flags #{:directed}}}
;; By default, graphs are not directed or strict.
{::graph {:edges [["a" "b"]]}}


;; ## Graph Attributes

;; Default attributes can be passed to set attributes for the full graph. The `:default-attributes` map takes attributes that are of type `:graph`, `:edge`, or `:node`.
{::graph {:default-attributes {:node {:penwidth "5.0"}
                               :graph {:label "My cool graph."}
                               :edge {:label "edge"}}
          :edges [["a" "b"]]}}

;; ## Attribute Descriptions

;; You can find more details about available attributes at [graphviz attributes](https://graphviz.org/doc/info/attrs.html). If an attribute for a node or edge does not have a default, then one must be provided via `:default-attributes`.

;; ## Subgraphs and Clusters

;; `clj-graphviz` also supports subgraphs and clusters. Subgraphs are just normal graphs which have a parent graph.
;; Subgraphs play three roles in graphviz:
;; - A subgraph can be used to indicate that certain nodes and edges should be grouped together.
;; - A subgraph can be used to specify attributes for a subset of nodes and edges.
;; - A cluster is used by some of the layout algorithms and drawing attributes.

;; Some other facts about subgraphs:
;; - subgraphs can reference nodes from their parent or sibling graphs
;; - subgraphs can be nested arbitrarily

;; ### Clusters

;; Clusters are special types of subgraphs. Subgraphs can be specied as clusters in two ways:
;; - implicitly, by giving the subgraph an id that starts with "cluster"
;; - explicitly, by setting the default graph attribute, "cluster", to "true".

;; ### Subgraph and Cluster Example

{::graph {:edges [["a" "b"]
                  ["a" "c"]
                  ["c" "b"]
                  ["c" "e"]]
          :default-attributes {:node {:penwidth "2.0"
                                      :color "blue"
                                      :fontsize "18"}}
          :subgraphs [{:edges [["a" "foo42"]
                               ["foo1" "foo2"]
                               ["foo1" "foo42"]]
                       :default-attributes {:node {:color "green"
                                                   :penwidth "5.0"
                                                   :fontsize "33"}
                                            :graph {:color "red"}}
                       :subgraphs
                       [{:edges [["foo1" "foofoo2"]
                                 ["foofoo1" "foofoo2"]
                                 ["foofoo1" "foofoo42"]]
                         :default-attributes {:node {:color "orange"
                                                     :fontsize "8"}
                                              :graph {:color "purple"
                                                      :cluster "true"}}}]
                       :id "cluster1"}
                      {:edges [["a" "bar42"]
                               ["bar1" "bar2"]
                               ["bar1" "bar42"]]
                       :default-attributes {:node {:color "green"
                                                   :fontsize "8"}}}]}}

{:nextjournal.clerk/visibility {:code :hide :result :hide}}


(comment
  ;; ### Node Attributes
  (clerk/with-viewer table-viewer-no-pagination
    (clerk/table
     (clerk/use-headers
      (into [["attribute" "default" "description"]]
            (for [{:keys [name default doc]} gspec/node-attributes]
              [name (if default
                      default
                      "") doc])))))
  ;; ### Edge Attributes
  (clerk/with-viewer table-viewer-no-pagination
    (clerk/table
     (clerk/use-headers
      (into [["attribute" "default" "description"]]
            (for [{:keys [name default doc]} gspec/edge-attributes]
              [name (if default
                      default
                      "") doc])))))

  ;; ### Graph Attributes
  (clerk/with-viewer table-viewer-no-pagination
    (clerk/table
     (clerk/use-headers
      (into [["attribute" "default" "description"]]
            (for [{:keys [name default doc]} gspec/graph-attributes]
              [name (if default
                      default
                      "") doc]))))))

{:nextjournal.clerk/visibility {:code :hide :result :hide}}


;; ## Render Options

;; `render-graph` also accepts a second argument with extra options.
;; - `:filename`: The name of the file to save the image to. (default graph.png)
;; - `:format`: One of `:png` `:jpeg` `:jpg` `:gif` `:svg` `:pdf` `:bmp` `:eps` `:ico` `:ps` `:ps2` `:tif` `:tiff` `:wbmp`. If not provided, will default to guessing based on the suffix of of `:filename`.
;; - `:layout-algorithm`: One of `:dot`, `:neato`, `:fdp`, `:sfdp`, `:twopi`, `:circo`, or `:patchwork`.

{:nextjournal.clerk/visibility {:code :hide :result :show}}
{::graph {:edges [["a" "b"]
                  ["a" "c"]
                  ["c" "b"]
                  ["c" "e"]]}
 ::opts {:filename "cool-graph"
         :format :jpg
         :layout-algorithm :neato}}





^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(comment
  (clerk/serve! {:watch-paths ["notebooks/docs.clj"]})
  
  (clerk/show! "notebooks/docs.clj")

  (clerk/build! {:paths ["notebooks/docs.clj"]
                 :out-path "docs/"
                 :bundle true})

  (render-graph {:nodes ["A" "B"]}
                {:filename "tmp/graph.png"})
  
  ,)
