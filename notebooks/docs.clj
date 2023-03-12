^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns docs
  (:require [nextjournal.clerk :as clerk]
            [com.phronemophobic.clj-graphviz :refer [render-graph]]
            [com.phronemophobic.clj-graphviz.spec :as gspec]
            [clojure.java.io :as io])
  (:import javax.imageio.ImageIO))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def graph-viewer
  {:pred ::graph
   :transform-fn
   (clerk/update-val
    
    (fn [m]

      (let [path (str "tmp/graph-" (hash m) ".png")]
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

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(clerk/add-viewers! [graph-viewer])

{:nextjournal.clerk/visibility {:code :hide :result :show}}

;; # clj-graphviz

;; The main entrypoint for `clj-graphviz`'s high level clojure api
;; is `render-graph`.

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
;; - `:nodes`: A sequence of node. Redundant if all nodes have edges.
;; - `:edges`: A sequences of tuples (from, to) of nodes.
;; - `:default-attributes`: A map of node types to their default attributes
;; - `:flags`: flags should be a subset of `#{:directed :strict}`

;; ### Graph Nodes

;; A graph node is either a node-id represented as a string or map.
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

;; String nodes and map nodes can be mixed
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

;; Default attributes can be passed to set attributes for the full graph. The default-attributes map takes attributes that are of type `:graph`, `:edge`, or `:node`.
{::graph {:default-attributes {:node {:penwidth "5.0"}
                               :graph {:label "My cool graph."}
                               :edge {:label "edge"}}
          :edges [["a" "b"]]}}

;; ## Attribute Descriptions

;; Below is table of the attributes for nodes, edges, and graphs. If an attribute for a node or edge does not have a default, then one must be provided via `:default-attributes`.

;; ### Node Attributes
(clerk/table
 (clerk/use-headers
  (into [["attribute" "default" "description"]]
        (for [{:keys [name default doc]} gspec/node-attributes]
          [name (if default
                  default
                  "") doc]))))
;; ### Edge Attributes
(clerk/table
 (clerk/use-headers
  (into [["attribute" "default" "description"]]
        (for [{:keys [name default doc]} gspec/edge-attributes]
          [name (if default
                  default
                  "") doc]))))

;; ### Graph Attributes
(clerk/table
 (clerk/use-headers
  (into [["attribute" "default" "description"]]
        (for [{:keys [name default doc]} gspec/graph-attributes]
          [name (if default
                  default
                  "") doc]))))



;; ## Render Options

;; `render-graph` also accepts a second argument with extra options.
;; - `:filename`: The name of the file to save the image to. (default graph.png)
;; - `:format`: One of `:png` `:jpeg` `:jpg` `:gif` `:svg` `:pdf` `:bmp` `:eps` `:ico` `:ps` `:ps2` `:tif` `:tiff` `:wbmp`. If not provided, will default to guessing based on the suffix of of `:filename`.
;; - `:layout-algorithm`: One of `:dot`, `:neato`, `:fdp`, `:sfdp`, `:twopi`, `:circo`, or `:patchwork.

{::graph {:edges [["a" "b"]
                  ["a" "c"]
                  ["c" "b"]]}
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
