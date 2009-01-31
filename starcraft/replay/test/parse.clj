(ns starcraft.replay.test.parse
  (:use [clojure.contrib.test-is])
  (:require [starcraft.replay.parse :as parse])
  (:import (java.nio ByteBuffer)))

;; Buffers are wrapped in functions to generate
;; new ones in every assertion and not continue
;; with the same buffer.

(deftest null-string
  (let [buf1 #(ByteBuffer/wrap (make-array Byte/TYPE 1))
        buf2 #(ByteBuffer/wrap (into-array Byte/TYPE (map byte [65 0 66])))]

    (is (= (parse/null-string (buf1) 0) "")
        "fetching 0 bytes returns an empty string")
    (is (= (parse/null-string (buf1) 1) "")
        "fetching 1 byte from an all-zero buffer returns an empty string")
    (is (nil? (parse/null-string (buf1) 2))
        "fetching more bytes than the buffer's length should return nil")
    
    (is (= (parse/null-string (buf2) 1) "A"))
    (is (= (parse/null-string (buf2) 2) "A"))
    (is (= (parse/null-string (buf2) 3) "A")
        "fetching should stop at the first NUL character")

    (let [b (buf2)]
      (parse/null-string b 3)
      (is (= (.position b) 3)
          "the buffer should be read until n, even if a NUL is found before the end."))               
        
    ))

(deftest read-field
  (let [buf #(ByteBuffer/wrap (into-array Byte/TYPE (map byte [65 0 66])))
        buf2 #(ByteBuffer/wrap (into-array Byte/TYPE (map byte [1 1 1 1])))]
    (is (nil? (parse/read-field (buf) 1 Character))
        "Invalid types should return nil.")
    
    (is (= (parse/read-field (buf) 1 String) "A"))
    (is (= (parse/read-field (buf) 2 String) "A"))
    (is (= (parse/read-field (buf) 3 String) "A"))

    (is (= (parse/read-field (buf2) 1 Byte) (byte 1))
        "reading one (1) piece of data returns it as a scalar")
    (is (= (parse/read-field (buf2) 2 Byte) [(byte 1) (byte 1)])
        "reading more than one (1) pieces of data returns a vector")
    (is (nil? (parse/read-field (buf2) 10 Byte))
        "reading more than the length of the buffer returns nil")

    (is (= (parse/read-field (buf2) 1 Short) (short 257)))
    (is (= (parse/read-field (buf2) 2 Short) [(short 257) (short 257)]))
    (is (nil? (parse/read-field (buf2) 4 Short)))

    (is (= (parse/read-field (buf2) 1 Integer) 0x1010101))
    
    ))

(deftest parse-buffer
  (let [buf #(ByteBuffer/wrap (into-array Byte/TYPE (map byte [2 3 4 5])))]
    (is (= {:byte 2 :3-bytes [3 4 5]}
           (parse/parse-buffer (buf)
                               [:byte 1 Byte]
                               [:3-bytes 3 Byte])))

    (is (= {}
           (parse/parse-buffer (buf)
                               [nil 4 Byte]))
        "nil field-name ignores the data")

    (is (= {:field [3 4]}
           (parse/parse-buffer (buf)
                               [:field [1 Byte] Byte]))
        "nested sized fields")

    (is (= {:field nil} (parse/parse-buffer (buf)
                                            [:field nil Byte]))
        "invalid nested size parameter")
    (is (= {:field nil} (parse/parse-buffer (buf)
                                            [:field [] Byte]))
        "invalid nested size parameter")
    
    ))
