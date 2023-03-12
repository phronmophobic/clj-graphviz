(ns com.phronemophobic.clj-graphviz.raw.cgraph
  (:require [com.phronemophobic.clong.gen.jna :as gen]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.rpl.specter :as specter])
  (:import java.lang.ref.Cleaner
           com.sun.jna.Pointer)
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

(def ^:no-doc libcgraph
  (com.sun.jna.NativeLibrary/getInstance "cgraph"))


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


(def structs-with-bitfields
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
              "raw"
              "cgraph"
              "api.edn")]
    (.mkdirs (.getParentFile outf))
    (with-open [w (io/writer outf)]
      (write-edn w
                 (-> ((requiring-resolve 'com.phronemophobic.clong.clang/easy-api)
                      "/opt/local/include/graphviz/cgraph.h")
                     simplify-rets
                     ignore-bitfielded-structs
                     fix-agdesc)))))

(def api
  #_(-> (clong/easy-api "/opt/local/include/graphviz/cgraph.h")
                     simplify-rets)
  (with-open [rdr (io/reader
                   (io/resource
                    "com/phronemophobic/clj-graphviz/raw/cgraph/api.edn"))
              rdr (java.io.PushbackReader. rdr)]
    (edn/read rdr)))

(gen/def-api libcgraph api)

;; workaround for bitfields
(defn set->Agdesc [flags]
  (let [masks {:directed 2r1
               :strict 2r10
               :maingraph 2r1000}]
    (int
     (reduce
      (fn [i flag]
        (bit-or i (get masks flag)))
      (:maingraph masks)
      flags))))

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


(defn make-cgraph [{:keys [nodes
                           edges
                           default-attributes
                           flags]
                    :as graph}]
  (let [graph-desc (set->Agdesc
                    (conj (clojure.set/intersection
                           flags
                           #{:directed :strict})
                          :maingraph))
        g* (agopen "" graph-desc nil)
        ;; add default attributes
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
             edge* (agedge g* from* to* "" 1)]
         (doseq [[k v] (dissoc edge :from :to)]
           (when-not (get-in default-attributes [:edge k])
             (throw (ex-info "Edge attributes must have defaults."
                             {:edge edge})))
           (agset edge* (name k) v))
         nodes*))
     nodes*
     edges)

    ;; be tidy
    (let [ptr (Pointer/nativeValue (.getPointer g*))]
      (.register cleaner g*
                 (fn []
                   (agclose (Pointer. ptr)))))

    g*))
