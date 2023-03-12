(ns com.phronemophobic.clj-graphviz.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [com.phronemophobic.clj-graphviz :as-alias g])
  (:gen-class))




(def node-attributes
  [{:name "distortion" :default "0.0" :doc "node distortion for shape=polygon"}
   {:name "fixedsize" :default "false" :doc "label text has no affect on node size"}
   {:name "fontname" :default "Times-Roman", :doc "font family"}
   {:name "fontsize", :default "14", :doc "point size of label"}
   {:name "group", :default nil, :doc "name of node’s group"}
   {:name "height", :default ".5", :doc "height in inches"}
   {:name "label", :default nil, :doc "name any string"}
   {:name "margin" :default "0.11" :doc "0.055 space between node label and boundary"}
   {:name "orientation", :default "0.0", :doc "node rotation angle"}
   {:name "peripheries" :default "shape" :doc "dependent number of node boundaries"}
   {:name "pin", :default "false", :doc "fix node at its pos attribute"}
   {:name "regular" :default "false" :doc "force polygon to be regular"}
   {:name "root" :default nil :doc "indicates node should be used as root of a layout"}
   {:name "shape", :default "ellipse", :doc "node shape"}
   {:name "shapefile" :default nil :doc "external EPSF or SVG custom shape file"}
   {:name "sides" :default "4" :doc "number of sides for shape=polygon"}
   {:name "skew" :default "0.0" :doc "skewing of node for shape=polygon"}
   {:name "width", :default ".75", :doc "width in inches"}
   {:name "z", :default "0.0", :doc "z coordinate for VRML output"}
   ;; decorative
   {:name "color", :default "black", :doc "node shape color"}
   {:name "fillcolor", :default "lightgrey", :doc "node fill color"}
   {:name "fontcolor", :default "black", :doc "text color"}
   {:name "layer", :default nil, :doc "range all, id or id:id"}
   {:name "nojustify", :default "false", :doc "context for justifying multiple lines of text"}
   {:name "style", :default nil, :doc "style options, e.g. bold, dotted, filled"}
   ])

;; edge attributes
(def edge-attributes
  [{:name "constraint", :default "true", :doc "use edge to affect node ranking"}
   {:name "fontname", :default "Times-Roman", :doc "font family"}
   {:name "fontsize", :default "14", :doc "point size of label"}
   {:name "headclip", :default "true", :doc "clip head end to node boundary"}
   {:name "headport", :default "center", :doc "position where edge attaches to head node"}
   {:name "label", :default nil, :doc "edge label"}
   {:name "len", :default "1.0/0.3", :doc "preferred edge length"}
   {:name "lhead", :default nil, :doc "name of cluster to use as head of edge"}
   {:name "ltail", :default nil, :doc "name of cluster to use as tail of edge"}
   {:name "minlen", :default "1", :doc "minimum rank distance between head and tail"}
   {:name "samehead", :default nil, :doc "tag for head node. edge heads with the same tag are merged onto the same port"}
   {:name "sametail", :default nil, :doc "tag for tail node edge tails with the same tag are merged onto the same port"}
   {:name "tailclip", :default "true", :doc "clip tail end to node boundary"}
   {:name "tailport", :default "center", :doc "position where edge attaches to tail node"}
   {:name "weight", :default "1", :doc "importance of edge"}

   ;; decorative
   {:name "arrowhead", :default "normal", :doc "style of arrowhead at head end"}
   {:name "arrowsize", :default "1.0", :doc "scaling factor for arrowheads"}
   {:name "arrowtail", :default "normal", :doc "style of arrowhead at tail end"}
   {:name "color", :default "black", :doc "edge stroke color"}
   {:name "decorate", :default nil, :doc "if set, draws a line connecting labels with their edges"}
   {:name "dir", :default "forward/none", :doc "forward, back, both, or none"}
   {:name "fontcolor", :default "black", :doc "type face color"}
   {:name "headlabel", :default nil, :doc "label placed near head of edge"}
   {:name "labelangle", :default "-25.0", :doc "angle in degrees which head or tail label is rotated off edge"}
   {:name "labeldistance", :default "1.0", :doc "scaling factor for distance of head or tail label from node"}
   {:name "labelfloat", :default "false", :doc "lessen constraints on edge label placement"}
   {:name "labelfontcolor", :default "black", :doc "type face color for head and tail labels"}
   {:name "labelfontname", :default "Times-Roman", :doc "font family for head and tail labels"}
   {:name "labelfontsize", :default "14", :doc "point size for head and tail labels"}
   {:name "layer", :default "overlay", :doc "range all, id or id:id"}
   {:name "nojustify", :default "false", :doc "context for justifying multiple lines of text"}
   {:name "style", :default nil, :doc "drawing attributes such as bold, dotted, or filled"}
   {:name "taillabel", :default nil, :doc "label placed near tail of edge"}
]
  
  )

;; graph attributes
(def graph-attributes
  [{:name "center", :default "false", :doc "† center drawing on page"}
   {:name "clusterrank", :default "local", :doc "may be global or none"}
   {:name "compound", :default "false", :doc "allow edges between clusters"}
   {:name "concentrate", :default "false", :doc "enables edge concentrators"}
   {:name "defaultdist", :default nil, :doc "spearation between nodes in different components"}
   {:name "dim", :default "2", :doc "dimension of layout"}
   {:name "dpi", :default "96", :doc "dimension of layout"}
   {:name "epsilon", :default nil, :doc "| or .0001 termination condition"}
   {:name "fontname", :default "Times-Roman", :doc "font family"}
   {:name "fontpath", :default nil, :doc "list of directories to such for fonts"}
   {:name "fontsize", :default "14", :doc "point size of label"}
   {:name "label", :default nil, :doc "any string"}
   {:name "margin", :default nil, :doc "space placed around drawing"}
   {:name "maxiter", :default nil, :doc "layout dependent bound on iterations in layout"}
   {:name "mclimit", :default "1.0", :doc "scale factor for mincross iterations"}
   {:name "mindist", :default "1.0", :doc "minimum distance between nodes"}
   {:name "mode", :default "major", :doc "variation of layout"}
   {:name "model", :default "shortpath", :doc "model used for distance matrix"}
   {:name "nodesep", :default ".25", :doc "separation between nodes, in inches"}
   {:name "nslimit", :default nil, :doc "if set to f, bounds network simplex iterations by (f) (number of nodes) when setting x-coordinates"}
   {:name "ordering", :default nil, :doc "specify out or in edge ordering"}
   {:name "orientation", :default "portrait", :doc "† use landscape orientation if rotate is not used and the value is landscape"}
   {:name "overlap", :default "true", :doc "specify if and how to remove node overlaps"}
   {:name "pack", :default nil, :doc "do components separately, then pack"}
   {:name "packmode", :default nil, :doc "node granularity of packing"}
   {:name "page", :default nil, :doc "unit of pagination, e.g. \"8.5,11\""}
   {:name "quantum", :default nil, :doc "if quantum > 0.0, node label dimensions will be rounded to integral multiples of quantum"}
   {:name "rank", :default nil, :doc "same , min, max, source or sink rankdir TB sense of layout, i.e, top to bottom, left to right, etc."}
   {:name "ranksep", :default ".75", :doc "separation between ranks, in inches."}
   {:name "ratio", :default nil, :doc "approximate aspect ratio desired, fill or auto"}
   {:name "remincross", :default nil, :doc "If true and there are multiple clusters, re-run cross-ing minimization"}
   {:name "resolution", :default nil, :doc "synonym for dpi"}
   {:name "root", :default nil, :doc "specifies node to be used as root of a layout"}
   {:name "rotate", :default nil, :doc "If 90, set orientation to landscape"}
   {:name "searchsize", :default "30", :doc "maximum edges with negative cut values to check when looking for a minimum one during network simplex"}
   {:name "sep", :default "0.1", :doc " factor to increase nodes when removing overlap"}
   {:name "size", :default nil, :doc "maximum drawing size, in inches"}
   {:name "splines", :default nil, :doc "render edges using splines"}
   {:name "start", :default "random", :doc "manner of initial node placement"}
   {:name "voro_margin", :default "0.05", :doc " factor to increase bounding box when more space is necessary during Voronoi adjustment"}
   {:name "viewport", :default nil, :doc "Clipping window"}

   ;; decorative
   {:name "bgcolor", :default nil, :doc "background color for drawing, plus initial fill color"}
   {:name "charset", :default "UTF-8", :doc "character encoding for text"}
   {:name "fontcolor", :default "black", :doc "type face color"}
   {:name "labeljust", :default "centered", :doc "left, right or center alignment for graph labels"}
   {:name "labelloc", :default "bottom", :doc "top or bottom location for graph labels"}
   {:name "layers", :default nil, :doc "names for output layers"}
   {:name "layersep", :default ":", :doc "separator characters used in layer specification"}
   {:name "nojustify", :default "false", :doc "context for justifying multiple lines of text"}
   {:name "outputorder", :default "breadthfirst", :doc "order in which to emit nodes and edges"}
   {:name "pagedir", :default "BL", :doc "traversal order of pages"}
   {:name "samplepoints", :default "8", :doc "number of points used to represent ellipses and circles on output"}
   {:name "stylesheet", :default nil, :doc "XML stylesheet"}
   {:name "truecolor", :default nil, :doc "determines truecolor or color map model for bitmap output"}
])

(def cluster-attributes
  [{:name "fontname", :default "Times-Roman", :doc "font family"}
   {:name "fontsize", :default "14", :doc "point size of label"}
   {:name "label", :default nil, :doc "edge label"}
   {:name "peripheries", :default "1", :doc "number of cluster boundaries"}
   ;; decorative
   {:name "bgcolor", :default nil, :doc "background color for cluster"}
   {:name "color", :default "black", :doc "cluster boundary color"}
   {:name "fillcolor", :default "black", :doc "cluster fill color"}
   {:name "fontcolor", :default "black", :doc "text color"}
   {:name "labeljust", :default "centered", :doc "left, right or center alignment for cluster labels"}
   {:name "labelloc", :default "top", :doc "top or bottom location for cluster labels"}
   {:name "nojustify", :default "false", :doc "context for justifying multiple lines of text"}
   {:name "pencolor", :default "black", :doc "cluster boundary color"}
   {:name "style", :default nil, :doc "style options, e.g. bold, dotted, filled"}
])

(def all-attributes
  {:node node-attributes
   :edge edge-attributes
   :graph graph-attributes
   ;; not sure how to use these yet
   ;; :cluster cluster-attributes
   })

(defn def-attribute* [type {:keys [name default doc]}]
  `(s/def ~(keyword (str "com.phronemophobic.clj-graphviz." (clojure.core/name type) ) name)
     string?))

(defn attribute-map* [])
(defn def-attribute-map* [type k attrs]
  `(s/def ~k
     (s/keys :opt-un ~(vec
                       (for [{:keys [name]} attrs]
                         (keyword (str "com.phronemophobic.clj-graphviz." (clojure.core/name type))
                                  name))))))

(doseq [[type attrs] all-attributes]
  (doseq [attr attrs]
    (eval (def-attribute* type attr)))
  (eval (def-attribute-map*
          type
          (keyword (str "com.phronemophobic.clj-graphviz")
                   (str (clojure.core/name type) "-attributes"))
         attrs)))

(def default-attributes
  (into {}
        (map (fn [[k attrs]]
               [k (into {}
                        (comp
                         (keep (fn [{:keys [name default]}]
                                 (when default
                                   [(keyword name) default]))))
                        attrs)]))
        all-attributes))

(s/def :com.phronemophobic.clj-graphviz.component/id string?)

(s/def ::g/node
  (s/or :simple string?
        :map (s/merge
              (s/keys :req-un [:com.phronemophobic.clj-graphviz.component/id])
              ::g/node-attributes)))


(s/def :com.phronemophobic.clj-graphviz.edge/from ::g/node)
(s/def :com.phronemophobic.clj-graphviz.edge/to ::g/node)

(s/def ::g/edge
  (s/or :simple (s/tuple ::g/node ::g/node)
        :map
        (s/merge
         (s/keys :req-un [:com.phronemophobic.clj-graphviz.edge/from
                          :com.phronemophobic.clj-graphviz.edge/to])
         ::g/edge-attributes)))

(s/def ::g/edges
  (s/coll-of ::g/edge))

(s/def ::g/nodes
  (s/coll-of ::g/node))

(s/def ::g/flags
  (s/coll-of
   #{:directed :strict}
      :into #{}))

(s/def :com.phronemophobic.clj-graphviz.default-attributes/node
  ::g/node-attributes)

(s/def :com.phronemophobic.clj-graphviz.default-attributes/edge
  ::g/edge-attributes)

(s/def :com.phronemophobic.clj-graphviz.default-attributes/graph
  ::g/graph-attributes)

(s/def ::g/default-attributes
  (s/keys :opt-un [:com.phronemophobic.clj-graphviz.default-attributes/node
                   :com.phronemophobic.clj-graphviz.default-attributes/edge
                   :com.phronemophobic.clj-graphviz.default-attributes/graph]))

(s/def ::g/graph
  (s/keys
   :opt-un [::g/edges
            ::g/nodes
            ::g/flags
            ::g/default-attributes]))


