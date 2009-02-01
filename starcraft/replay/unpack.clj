(ns starcraft.replay.unpack
  (:require [starcraft.replay.parse :as parse])
  (:import (hu.belicza.andras.bwhf.control BinReplayUnpacker)
           (java.io File)
           (java.nio ByteBuffer ByteOrder)
           (java.util Date)))


(defn decode-player-data
  "Decode a single player's data into a map."
  [buf]
  (parse/parse-buffer buf
    [:player-number  1 Integer]
    [:slot-number    1 Integer]
    [:type           1 Byte #({0 nil, 1 :cpu, 2 :human} (int %))]
    [:race           1 Byte #({0 "Zerg", 1 "Terran", 2 "Protoss"} (int %))]
    [nil             1 Byte]
    [:name          25 String]))

(defn decode-players-data
  [data]
  (let [players (partition 36 data)]
    (map (fn [vec]
           (let [buf (ByteBuffer/wrap (into-array Byte/TYPE vec))]
             (.order buf ByteOrder/LITTLE_ENDIAN)
             (decode-player-data buf)))
         players)))


(defn decode-headers
  [buf]
  (parse/parse-buffer buf
    [:game-engine         1 Byte]
    [:game-frames         1 Integer]
    [nil                  3 Byte]
    [:save-time           1 Integer #(Date. (long (* 1000 %)))]
    [nil                 12 Byte]
    [:game-name          28 String]
    [:map-width           1 Short]
    [:map-height          1 Short]
    [nil                 16 Byte]
    [:creator-name       24 String]
    [nil                  1 Byte]
    [:map-name           26 String]
    [nil                 38 Byte]
    [:players           432 Byte decode-players-data]
    [:player-spot-color   8 Integer]
    [:player-spot-index   8 Byte]))


;; The replay id is the first 4 bytes and is always 0x53526572
(defn unpack-replay-id
  [unpacker]
  (Integer/reverseBytes
   (.getInt
    (ByteBuffer/wrap
     (.unpackSection unpacker 4)))))

;; The headers are the first 0x279 bytes following the replay-id
(defn unpack-headers
  [unpacker]
  (.order (ByteBuffer/wrap (.unpackSection unpacker 0x279))
          ByteOrder/LITTLE_ENDIAN))

(defn unpack-commands
  [unpacker]
  (let [commands-length (Integer/reverseBytes
                         (.getInt
                          (ByteBuffer/wrap (.unpackSection unpacker 4))))]
    (.order (ByteBuffer/wrap (.unpackSection unpacker commands-length))
            ByteOrder/LITTLE_ENDIAN)))
    

(defn unpack
  "Unpack a replay file."
  [#^File f]
  (let [unpacker (BinReplayUnpacker. f)
        m {:replay-id (unpack-replay-id unpacker)
           :headers (decode-headers (unpack-headers unpacker))
           :commands (unpack-commands unpacker)
           }]
    (.close unpacker) ; needs to be closed manually.
    m))
