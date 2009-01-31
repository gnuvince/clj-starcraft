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
    [:player-number  1 :dword]
    [:slot-number    1 :dword]
    [:type           1 :byte #({0 nil, 1 :cpu, 2 :human} (int %))]
    [:race           1 :byte #({0 "Zerg", 1 "Terran", 2 "Protoss"} (int %))]
    [nil             1 :byte]
    [:name          25 :string]))

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
    [:game-engine         1 :byte]
    [:game-frames         1 :dword]
    [nil                  3 :byte]
    [:save-time           1 :dword #(Date. (long (* 1000 %)))]
    [nil                 12 :byte]
    [:game-name          28 :string]
    [:map-width           1 :word]
    [:map-height          1 :word]
    [nil                 16 :byte]
    [:creator-name       24 :string]
    [nil                  1 :byte]
    [:map-name           26 :string]
    [nil                 38 :byte]
    [:players           432 :byte decode-players-data]
    [:player-spot-color   8 :dword]
    [:player-spot-index   8 :byte]))


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


(defn unpack
  "Unpack a replay file."
  [#^File f]
  (let [unpacker (BinReplayUnpacker. f)]
    {:replay-id (unpack-replay-id unpacker)
     :headers (unpack-headers unpacker)
     }))
