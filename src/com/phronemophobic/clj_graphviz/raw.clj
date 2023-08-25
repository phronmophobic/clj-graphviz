(ns com.phronemophobic.clj-graphviz.raw
  (:require [com.phronemophobic.clong.gen.jna :as gen]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [com.rpl.specter :as specter])
  (:import java.lang.ref.Cleaner
           com.sun.jna.Pointer
           com.sun.jna.Structure)
  (:gen-class))

(def cleaner (Cleaner/create))

(defn ^:private write-edn [w obj]
  (binding [*print-length* nil
            *print-level* nil
            *print-dup* false
            *print-meta* false
            *print-readably* true

            ;; namespaced maps not part of edn spec
            *print-namespace-maps* false

            *out* w]
    (pr obj)))

(try
  (require 'com.phronemophobic.cljonda.graphviz)
  (catch java.io.FileNotFoundException e
    nil))

(def ^:no-doc libgvc
  (com.sun.jna.NativeLibrary/getInstance "gvc"))

(def ^:private simple-rets
  #{[:coffi.mem/pointer :clong/Agraph_s]
    [:coffi.mem/pointer :clong/Agedge_s]
    [:coffi.mem/pointer :clong/Agnode_s]})
(defn ^:private simplify-rets [api]
  ;; Change all Agraph_t* return types to pointer
  (specter/setval [:functions
                   specter/ALL
                   :function/ret
                   #(contains? simple-rets %)]
                  :coffi.mem/pointer
                  api))

(defn ^:private simplify-fields [api]
  ;; Change all Agraph_t* return types to pointer
  (specter/setval [:structs
                   specter/ALL
                   :fields
                   specter/ALL
                   :datatype
                   #(contains? simple-rets %)]
                  :coffi.mem/pointer
                  api))

(def ^:private structs-with-bitfields
  #{:clong/Agtag_s
    :clong/Agdesc_s})
(defn ^:private ignore-bitfielded-structs [api]
  (specter/setval
   [:structs
    specter/ALL
    (fn [struct]
      (structs-with-bitfields (:id struct)))]
   specter/NONE
   api))

;; another workaround for bitfields
;; not supported in clong
(defn ^:private fix-agdesc [api]
  (specter/setval
   [:functions
    specter/ALL
    :function/args
    specter/ALL
    #(= :clong/Agdesc_s %)]
   :coffi.mem/int
   api))

(defn ^:private dump-api []
  (let [outf (io/file
              "resources"
              "com"
              "phronemophobic"
              "clj-graphviz"
              "api.edn")]
    (.mkdirs (.getParentFile outf))
    (with-open [w (io/writer outf)]
      (write-edn w
                 ((requiring-resolve 'com.phronemophobic.clong.clang/easy-api)
                  "/opt/local/include/graphviz/gvc.h")
                 ))))


(def raw-api
  #_((requiring-resolve 'com.phronemophobic.clong.clang/easy-api) "/opt/local/include/graphviz/gvc.h")
  
  (with-open [rdr (io/reader
                     (io/resource
                      "com/phronemophobic/clj-graphviz/api.edn"))
                rdr (java.io.PushbackReader. rdr)]
      (edn/read rdr)))

(def api
  (-> raw-api
      simplify-rets
      simplify-fields
      ;; ignore-bitfielded-structs
      ;; fix-agdesc
      ))

(gen/def-api libgvc api)

(defn ^:private struct-field [struct-name field-name]
  (->> api
       :structs
       (filter #(= (keyword "clong" struct-name)
                   (:id %)))
       first
       :fields
       (filter #(= field-name
                   (:name %)))
       first))

(defmulti ^:private read-field*
  (fn [ptr field]
    (let [datatype (:datatype field)]
      (if (and (vector? datatype)
               (= :coffi.mem/pointer
                  (first datatype)))
        :coffi.mem/pointer
        datatype))))

(defmethod read-field* :coffi.mem/long [ptr field]
  (.getLong ptr
            #_(:calculated-offset field)
            (long
             (/ (:calculated-offset field)
                8))))

(defmethod read-field* :coffi.mem/pointer [ptr field]
  (.getPointer ptr
               (long
                 (/ (:calculated-offset field)
                    8))))

(defn ^:private read-field
  ([ptr field-name]
   (.readField ^Structure ptr field-name))
  ([ptr struct-name field-name]
   (let [field (struct-field struct-name field-name)]
     (read-field* ptr field))))

(defn agtype [ptr]
  ;; All objects have an Agobj_s structs as their first field.
  ;; All Agobj_s structs have an Agtag_s as their first field
  ;; All Agtag_s structs have an `objtype:2` bitfield as their first field.
  ;; Ergo, the object tag is the first two bits of an object's struct.
  (let [tag (bit-and 2r11
                     (.getByte ^Pointer ptr 0))]
    (case tag
      0 :graph
      1 :node
      (2 3) :edge)))

(let [obj-data-field (struct-field "Agobj_s" "data")]
 (defn aginfo [ptr]
   (let [type (agtype ptr)
         data-ptr (read-field* ptr obj-data-field)
         info
         (case type
           :graph (Structure/newInstance com.phronemophobic.clj_graphviz.raw.structs.Agraphinfo_t data-ptr)
           :node (Structure/newInstance com.phronemophobic.clj_graphviz.raw.structs.Agnodeinfo_t data-ptr)
           :edge (Structure/newInstance com.phronemophobic.clj_graphviz.raw.structs.Agedgeinfo_t data-ptr))]
     info)))


;; workaround for bitfields
(defn set->Agdesc [flags]
  (let [masks {:directed 2r1
               :strict 2r10
               :maingraph 2r1000}
        i (int
           (reduce
            (fn [i flag]
              (bit-or i (get masks flag)))
            (:maingraph masks)
            flags))
        desc (com.phronemophobic.clj_graphviz.raw.structs.Agdesc_s.)]
    (.setInt (.getPointer desc) 0 i)
    (.read desc)
    desc))

;; should match definitions
;; (.getInt (.getGlobalVariableAddress libcgraph "Agdirected")
;;          0)
;; (.getInt (.getGlobalVariableAddress libcgraph "Agstrictdirected")
;;          0)
;; (.getInt (.getGlobalVariableAddress libcgraph "Agundirected")
;;          0)
;; (.getInt (.getGlobalVariableAddress libcgraph "Agstrictundirected")
;;          0)

;; Agdesc_t Agdirected = { .directed = 1, .maingraph = 1 };
;; Agdesc_t Agstrictdirected = { .directed = 1, .strict = 1, .maingraph = 1 };
;; Agdesc_t Agundirected = { .maingraph = 1 };
;; Agdesc_t Agstrictundirected = { .strict = 1, .maingraph = 1 };
(def Agdirected (set->Agdesc #{:directed :maingraph}))
(def Agstrictdirected (set->Agdesc #{:directed :strict :maingraph}))
(def Agundirected (set->Agdesc #{ :maingraph}))
(def Agstrictundirected (set->Agdesc #{ :strict  :maingraph}))

;; object tags
(def AGRAPH 0)
(def AGNODE 1)
(def AGOUTEDGE 2)
(def AGINEDGE 3)
(def AGEDGE AGOUTEDGE)

(def ^:private kw->node-type
  {:node AGNODE
   :graph AGRAPH
   :edge AGEDGE})

(defn ^:private normalize-node [n]
  (if (string? n)
    {:id n}
    ;; assume map
    n))

(defn ^:private make-node [g* default-attributes node]
  (if-let [node-id (:id node)]
    (let [node* (agnode g* node-id 1)]
      (reduce
       (fn [node* [k v]]
         (when-not (get-in default-attributes [:node k])
           (throw (ex-info "Node attributes must have defaults."
                           {:node node})))
         (agset node* (name k) v)
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

(declare make-subgraph)
(defn ^:private apply-graph-properties!
  "Applies graph properties. Returns accumulated nodes*."
  [g*
   {:keys [nodes
           edges
           subgraphs
           default-attributes]}
   ;; existing nodes
   ;; used for subgraphs
   nodes*]
  (let [;; add default attributes
        g* (reduce
            (fn [g* [node-type attrs]]
              (let [nt (kw->node-type node-type)]
                (when-not nt
                  (throw (ex-info "Invalid node type."
                                  {:node-type node-type})))
                (reduce
                 (fn [g* [k v]]
                   (agattr g* (kw->node-type node-type)
                           (name k) v)
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
         (or nodes* {})
         nodes)

        ;; add edges
        nodes*
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
                 edge* (agedge g* from* to* (str (gensym)) 1)]
             (doseq [[k v] (dissoc edge :from :to)]
               (when-not (get-in default-attributes [:edge k])
                 (throw (ex-info "Edge attributes must have defaults."
                                 {:edge edge})))
               (agset edge* (name k) v))
             nodes*))
         nodes*
         edges)

        ;; add subgraphs
        nodes* (reduce (fn [nodes* subgraph]
                         (make-subgraph g* subgraph nodes*))
                       nodes
                       subgraphs)]


    nodes*))

(defn ^:private make-subgraph
  "Creates a subgraph of g*."
  [g*
   {:keys [id
           nodes
           edges
           default-attributes]
    :as subgraph}
   nodes*]
  (let [sg* (agsubg g* (or id (name (gensym "subg_"))) 1)
        nodes* (apply-graph-properties! sg* subgraph nodes*)]
    ;; note subgraphs are automatically freed
    ;; when calling agclose on the subraph's parent.
    ;; (I think).

    nodes*))

(defn make-cgraph [{:keys [id
                           nodes
                           edges
                           subgraphs
                           default-attributes
                           flags]
                    :as graph}]
  (let [graph-desc (set->Agdesc
                    (conj (clojure.set/intersection
                           flags
                           #{:directed :strict})
                          :maingraph))
        g* (agopen (or id "") graph-desc nil)
        nodes* (apply-graph-properties! g* graph {})
        ]

    (let [ptr (Pointer/nativeValue g*)]
      (.register cleaner g*
                 (fn []
                   (agclose (Pointer. ptr)))))

    g*))


(defn node-eduction [g*]
  (eduction
   (take-while some?)
   (iterate #(agnxtnode g* %)
            (agfstnode g*))))

(defn ^:private distinct-by
  "Returns a lazy sequence of the elements of coll with duplicates removed.
  Returns a stateful transducer when no collection is provided."
  {:added "1.0"
   :static true}
  ([keyfn]
   (fn [rf]
     (let [seen (volatile! #{})]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result input]
          (let [k (keyfn input)]
           (if (contains? @seen k)
             result
             (do (vswap! seen conj k)
                 (rf result input))))))))))

(defn edge-eduction [g*]
  (eduction
   (comp (take-while some?)
         (mapcat (fn [node]
                   (sequence
                    (take-while some?)
                    (iterate #(agnxtedge g* % node)
                             (agfstedge g* node)))))
         (distinct-by agnameof))
   (node-eduction g*)))

(defn ->node [node*]
  (let [info (aginfo node*)
        coord (read-field info "coord")]
    {:width (read-field info "width")
     :height (read-field info "height")
     :id (agnameof node*)
     :x (read-field coord "x")
     :y (read-field coord "y")}))

(defn ->nodes [g*]
  (into []
        (map ->node)
        (node-eduction g*)))

(defn ->pointf [pointf]
  {:x (read-field pointf "x")
   :y (read-field pointf "y")})

(defn ->boxf [boxf]
  {:lower-left (->pointf (read-field boxf "LL"))
   :upper-right (->pointf (read-field boxf "UR"))})

(defn ->bezier [bezier]
  (let [
        ;; points (read-field bezier "list")
        size (read-field bezier "size")
        pt-array (.toArray (read-field bezier "list") size)
        sflag (read-field bezier "sflag")
        eflag (read-field bezier "eflag")]
    (merge
     {:points (mapv ->pointf pt-array)}
     ;; If bp points to a bezier structure and the bp->sflag
     ;; field is true, there should be an arrowhead attached to the beginning of the bezier.
     ;; The field bp->sp gives
     ;; the point where the nominal tip of the arrowhead would touch the tail node. (If there is no arrowhead,
     ;; bp->list[0] will touch the node.) Thus, the length and direction of the arrowhead is determined by the
     ;; vector going from bp->list[0] to bp->sp.
     (when (not (zero? sflag))
       {:arrow-start (->pointf (read-field bezier "sp"))})
     ;; Analogously, an arrowhead at the head node is specified
     ;; by bp->eflag and the vector from bp->list[bp->size-1] to bp->ep.
     (when (not (zero? eflag))
       {:arrow-end (->pointf (read-field bezier "ep"))}))))

(defn ->splines [spline]
  (let [size (read-field spline "size")
        bezier (read-field spline "list")
        bezier-arr (.toArray bezier size)]
    {;; This seems to always empty?
     ;; :bounding-box (->boxf (read-field spline "bb"))
     :beziers (mapv ->bezier bezier-arr)}))

(defn ->edge [edge*]
  (let [info (aginfo edge*)]
    (->splines (read-field info "spl"))))


(defn ->edges [g*]
  (into []
        (map ->edge)
        (edge-eduction g*)))
