(ns g-path.core
  (:import
   (java.nio.file Paths)
   (sun.nio.fs UnixPath)
   (java.io File)
   (java.net URL))
  (:require
   [clojure.java.io :as io]))

;; UnixPath

(def dispatch-fn-with-vargs (fn df [v & _args] (class v)))

(defmulti ->path dispatch-fn-with-vargs)
(defmethod ->path String [v & args] (-> (Paths/get v (into-array String args))))
(defmethod ->path UnixPath [v & _args] v)
(defmethod ->path File [v & _args] (-> v (.getPath) (->path)))
(defmethod ->path URL [v & _args] (->path (.getPath ^URL v)))

(defn normalize [^UnixPath path] (.normalize path))
#_ (-> (io/file ".") ->path normalize)

(defn relativize [^UnixPath a ^UnixPath b] (.relativize a b))
#_ (relativize (->path "/") (->path "/" "tmp" "tmp"))

(defn relativize-any [a b] (relativize (->path a) (->path b)))

(def current-path
  (->path (System/getProperty "user.dir")))

(defmulti ->absolute-path class)
(defmethod ->absolute-path File [v] (->path (.getAbsolutePath ^File v)))
(defmethod ->absolute-path URL [v] (->path v))

(defmulti ->relative-path class)
(defmethod ->relative-path UnixPath [v] (relativize current-path v))
(defmethod ->relative-path File [v] (->relative-path (->absolute-path v)))
(defmethod ->relative-path URL [v] (->relative-path (->path v)))

(comment
 (->path "/" "tmp" "tmp")
 (normalize (->absolute-path (io/file ".")))

 (->absolute-path (io/file "."))
 (->absolute-path (io/resource "notebooks"))

 (->relative-path (io/file "."))
 (->relative-path (io/resource "notebooks"))
 (->relative-path (->absolute-path (io/file "app"))))
