(ns com.phronemophobic.clj-graphviz.raw.gvc
  (:require [com.phronemophobic.clong.clang :as clong]
            [com.phronemophobic.clong.gen.jna :as gen]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]])
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

(def ^:no-doc libgvc
  (com.sun.jna.NativeLibrary/getInstance "gvc"))

(defn dump-api []
  (let [outf (io/file
              "resources"
              "com"
              "phronemophobic"
              "clj-graphviz"
              "raw"
              "gvc"
              "api.edn")]
    (.mkdirs (.getParentFile outf))
    (with-open [w (io/writer outf)]
      (write-edn w
                 (clong/easy-api "/opt/local/include/graphviz/gvc.h")
                 ))))

(def api
  #_(clong/easy-api "/opt/local/include/graphviz/gvc.h")
  (with-open [rdr (io/reader
                   (io/resource
                    "com/phronemophobic/clj-graphviz/raw/gvc/api.edn"))
              rdr (java.io.PushbackReader. rdr)]
    (edn/read rdr)))

(gen/def-api libgvc api)
