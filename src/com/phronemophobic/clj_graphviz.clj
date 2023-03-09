(ns com.phronemophobic.clj-graphviz
  (:require [com.phronemophobic.clj-graphviz.raw.cgraph :as cgraph]
            [com.phronemophobic.clj-graphviz.raw.gvc :as gvc]
            [clojure.java.io :as io]
            [clojure.string :as str]
            clojure.set)
  (:gen-class))

(def supported-formats
  #{:png
    :jpeg
    :jpg
    :gif})
(def supported-layout-algorithms
  #{:dot
    :neato
    :fdp
    :sfdp
    :twopi
    :circo
    :patchwork})

(defn guess-format
  "Trieds to guess an image format based off of a filename"
  [fname]
  (some (fn [format]
          (when (str/ends-with? fname (name format))
            format))
        supported-formats))

(defn render-graph
  "Layout a graph and save an image to `filename`.
  
  A graph is map of the keys: `:nodes`, `:edges`, and `:flags`.
  `:nodes`: A sequence of node ids (strings). Redundant if all nodes have edges.
  `:edges`: A sequences of tuples (from, to) of node ids.
  `:flags`: flags should be a subset of #{:directed :strict}

  Options is map of the keys: `:filename`, `:format`, `:layout-algorithm`.
  `:filename`: The name of the file to save the image to. (default graph.png)
  `:format`: One of :png, :jpeg, :jpg, or :gif
  `:layout-algorithm`: one of the following:
      `:dot` (default) A Sugiyama-style hierarchical layout [STT81, GKNV93].
      `:neato` A “symmetric” layout algorithm based on stress reduction. This is a variation of multidimensional
               scaling [KS80, Coh87]. The default implementation uses stress majorization [GKN04]. An alternate
               implementation uses the Kamada-Kawai algorithm [KK89]
      `:fdp` An implementation of the Fruchterman-Reingold force-directed algorithm [FR91] for “symmetric”
             layouts. This layout is similar to neato, but there are performance and feature differences.
      `:sfdp` A multiscale force-directed layout using a spring-electrical model [Hu05].
      `:twopi` A radial layout as described by Wills [Wil97].
      `:circo` A circular layout combining aspects of the work of Six and Tollis [ST99, ST00] and Kaufmann and
               Wiese [KW].
      `:patchwork` An implementation of squarified treemaps [BHvW00]."
  ([{:keys [nodes
            edges
            flags]
     :as graph}]
   (render-graph graph {}))
  ([{:keys [nodes
            edges
            flags]
     :as graph}
    {:keys [filename
            format
            layout-algorithm]
     :as options}]
   (let [filename (or filename
                      "graph.png")
         format (or format
                    (guess-format filename))
         layout-algorithm (or layout-algorithm
                              :dot)]
     (when-not (supported-formats format)
       (throw (ex-info
               (str "Unsupported format. Must be one of "
                    (str/join ", " supported-formats))
               {:format format})))
     (when-not (supported-layout-algorithms layout-algorithm)
       (throw (ex-info
               (str "Unsupported layout algorithm. Must be one of "
                    (str/join ", " supported-layout-algorithms))
               {:layout-algorithm layout-algorithm})))
     (let [graph-desc (cgraph/set->Agdesc
                       (conj (clojure.set/intersection
                              flags
                              #{:directed :strict})
                             :maingraph))
           g* (cgraph/agopen "" graph-desc nil)

           nodes*
           (reduce
            (fn [nodes* node-id]
              (when-not (string? node-id)
                (throw (ex-info "Node ids must be strings"
                                {:node-id node-id})))
              (assert (string? node-id)
                      "Node ids must be strings.")
              (let [node* (or (get nodes* node-id)
                              (cgraph/agnode g* node-id 1))]
                (assoc nodes* node-id node*)))
            {}
            nodes)]
       (reduce
        (fn [nodes* [from to]]
          (doseq [node-id [from to]]
            (when-not (string? node-id)
              (throw (ex-info "Node ids must be strings"
                              {:node-id node-id}))))
          (let [from* (or (get nodes* from)
                          (cgraph/agnode g* from 1))
                nodes* (assoc nodes* from from*)

                to* (or (get nodes* to)
                        (cgraph/agnode g* to 1))
                nodes* (assoc nodes* to to*)]
            (cgraph/agedge g* from* to* "" 1)
            nodes*))
        nodes*
        edges)

       (let [gvc (gvc/gvContext)]
         (gvc/gvLayout gvc g* (name layout-algorithm))
         (gvc/gvRenderFilename gvc g* (name format) filename)

         (gvc/gvFreeLayout gvc g*)
         (cgraph/agclose g*)
         (gvc/gvFreeContext gvc)
         nil)))))

render-graph
