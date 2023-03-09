(ns com.phronemophobic.clj-graphviz.raw.cgraph
  (:require [com.phronemophobic.clong.gen.jna :as gen]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.rpl.specter :as specter])
  (:gen-class))

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
                     simplify-rets)))))

(def api
  #_(-> (clong/easy-api "/opt/local/include/graphviz/cgraph.h")
                     simplify-rets)
  (with-open [rdr (io/reader
                   (io/resource
                    "com/phronemophobic/clj-graphviz/raw/cgraph/api.edn"))
              rdr (java.io.PushbackReader. rdr)]
    (edn/read rdr)))

(gen/def-api libcgraph api)

(defn set->Agdesc [flags]
  (reduce
   (fn [agdesc flag]
     (doto agdesc
       (.writeField (name flag) (int 1))))
   (com.phronemophobic.clj_graphviz.raw.cgraph.structs.Agdesc_s.)
   flags))

;; Agdesc_t Agdirected = { .directed = 1, .maingraph = 1 };
;; Agdesc_t Agstrictdirected = { .directed = 1, .strict = 1, .maingraph = 1 };
;; Agdesc_t Agundirected = { .maingraph = 1 };
;; Agdesc_t Agstrictundirected = { .strict = 1, .maingraph = 1 };
(def Agdirected (set->Agdesc #{:directed :maingraph}))
(def Agstrictdirected (set->Agdesc #{:directed :strict :maingraph}))
(def Agundirected (set->Agdesc #{ :maingraph}))
(def Agstrictundirected (set->Agdesc #{ :strict  :maingraph}))
