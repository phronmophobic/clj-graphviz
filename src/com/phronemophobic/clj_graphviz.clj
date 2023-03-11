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
    :gif
    :svg
    :pdf
    :bmp
    :eps
    :ico
    :ps
    :ps2
    :tif
    :tiff
    :wbmp})
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

(def ^:private kw->node-type
  {:node cgraph/AGNODE
   :graph cgraph/AGRAPH
   :edge cgraph/AGEDGE})


(defn ^:private normalize-node [n]
  (if (string? n)
    {:id n}
    ;; assume map
    n))

(defn ^:private make-node [g* default-attributes node]
  (if-let [node-id (:id node)]
    (let [node* (cgraph/agnode g* node-id 1)]
      (reduce
       (fn [node* [k v]]
         (when-not (get-in default-attributes [:node k])
           (throw (ex-info "Node attributes must have defaults."
                           {:node node})))
         (cgraph/agset node* k v)
         node*)
       node*
       (dissoc node :id)))
    ;; else
    (throw
     (ex-info "Missing node :id"
              {:node node}))))

(defn ^:private normalize-edge [e]
  (if (vector? e)
    {:from (first e)
     :to (second e)}
    ;; assume map
    e))


(defn render-graph
  "Layout a graph and save an image to `filename`.
  
  A graph is map of the keys: `:nodes`, `:edges`, and `:flags`.
  `:nodes`: A sequence of node ids (strings). Redundant if all nodes have edges.
  `:edges`: A sequences of tuples (from, to) of node ids.
  `:flags`: flags should be a subset of #{:directed :strict}

  Options is map of the keys: `:filename`, `:format`, `:layout-algorithm`.
  `:filename`: The name of the file to save the image to. (default graph.png)
  `:format`: One of `:png` `:jpeg` `:jpg` `:gif` `:svg` `:pdf` `:bmp` `:eps` `:ico` `:ps` `:ps2` `:tif` `:tiff` `:wbmp` 
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
            default-attributes
            flags]
     :as graph}]
   (render-graph graph {}))
  ([{:keys [nodes
            edges
            default-attributes
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
           ;; add default attributes
           g* (reduce
               (fn [g* [node-type attrs]]
                 (let [nt (kw->node-type node-type)]
                   (when-not nt
                     (throw (ex-info "Invalid node type."
                                     {:node-type node-type})))
                   (reduce
                    (fn [g* [k v]]
                      (cgraph/agattr g* (kw->node-type node-type)
                                     k v)
                      g*)
                    g*
                    attrs)))
               g*
               default-attributes)

           ;; add nodes
           nodes*
           (reduce
            (fn [nodes* node]
              (when-not (or (string? node)
                            (map? node))
                (throw (ex-info "Nodes must be strings or maps."
                                {:node node})))
              (let [node (normalize-node node)
                    node-id (:id node)
                    node* (or (get nodes* node-id)
                              (make-node g* default-attributes node))]
                (assoc nodes* node-id node*)))
            {}
            nodes)]

       ;; add edges
       (reduce
        (fn [nodes* edge]
          (let [edge (normalize-edge edge)
                {:keys [from to]} edge

                from-node (normalize-node from)
                from-id (:id from-node)
                from* (or (get nodes* from-id)
                          (make-node g* default-attributes from-node))
                nodes* (assoc nodes* from-id from*)

                to-node (normalize-node to)
                to-id (:id to-node)
                to* (or (get nodes* to-id)
                        (make-node g* default-attributes to-node))
                nodes* (assoc nodes* to-id to*)
                edge* (cgraph/agedge g* from* to* "" 1)]
            (doseq [[k v] (dissoc edge :from :to)]
              (when-not (get-in default-attributes [:edge k])
                (throw (ex-info "Edge attributes must have defaults."
                                {:edge edge}))
                (cgraph/agset edge* k v)))
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

