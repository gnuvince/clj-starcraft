(ns starcraft.replay.parse
  (:import (hu.belicza.andras.bwhf.control BinReplayUnpacker)
           (java.io File)
           (java.nio ByteBuffer ByteOrder)
           (java.util Date)))

(defn null-string
  "Read a nul-terminated string. Stop at \\0 or at length n,
  whichever comes first. Return nil if trying to read too much."
  [buf n]
  (try
   ; use doall to read the buffer non-lazily.
   (let [bytes (doall (for [_ (range n)] (char (.get buf))))]
     (apply str (take-while #(not= % \u0000) bytes)))
   (catch RuntimeException e nil)))

(defn- read-field-aux
  [buf n type]
  (try
   (let [f ({Byte    (memfn get)
             Short   (memfn getShort)
             Integer (memfn getInt)} type)
         vec (into [] (for [_ (range n)] (f buf)))]
     (if (= n 1)
       (first vec)
       vec))
   (catch RuntimeException e nil)))
  

(derive String  ::string)
(derive Byte    ::integer)
(derive Short   ::integer)
(derive Integer ::integer)

(defmulti read-field
  "Read `size` units of type `type` from buf.
  `buf` : a java.nio.ByteBuffer
  `size`: an integer or a vector of the form [integer type]
  `type`: String, Byte, Short or Integer.

  Return nil if reading too much data, passed an invalid
  size or type."
  (fn [buf size type] [(vector? size) type]))

(defmethod read-field :default [& args] nil)

(defmethod read-field [false ::string]
  [buf size type]
  (null-string buf size))

(defmethod read-field [false ::integer]
  [buf size type]
  (read-field-aux buf size type))

(defmethod read-field [true ::string]
  [buf [n type-aux] type]
  (let [size (read-field-aux buf n type-aux)]
    (null-string buf size)))

(defmethod read-field [true ::integer]
  [buf [n type-aux] type]
  (let [size (read-field-aux buf n type-aux)]
    (read-field-aux buf size type)))


(defn parse-buffer
  "A v-form is a vector of the form: [:field-name length Type func?]
  Each v-form is read from buf and the whole data is return as a map.
  If a field-name is nil, the data is not returned (but the field is
  read nonetheless to move forward into the buffer."
  [buf & v-forms]
  (reduce (fn [m [field-name size type func]]
            (let [data (read-field buf size type)]
              (if (nil? field-name)
                m
                (assoc m field-name
                       (if (nil? func)
                         data
                         (func data))))))
          {}
          v-forms))
