(ns starcraft.replay.unpack
  (:use [starcraft.replay.parse]
        [starcraft.replay.actions])
  (:import (hu.belicza.andras.bwhf.control BinReplayUnpacker)
           (java.io File)
           (java.nio ByteBuffer ByteOrder)
           (java.util Date)))

(defn decode-player-data
  "Decode a single player's data into a map."
  [#^ByteBuffer buf]
  (parse-buffer buf
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
  [#^ByteBuffer buf]
  (parse-buffer buf
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

(defn decode-command-block
  [#^ByteBuffer buf cmd-size tick]
  (let [end (+ cmd-size (.position buf))]
    (loop [v []]
      (if (= (.position buf) end)
        v
        (let [player-id (.get buf)
              action-id (.get buf)
              {:keys [name fields]} (*actions* (int action-id))
              action (apply parse-buffer buf fields)]
          (recur (conj v (merge {:tick tick
                                 :name name
                                 :player-id player-id}
                                 action))))))))

         
(defn decode-commands
  [#^ByteBuffer buf]
  (loop [cmds []]
    (if (.hasRemaining buf)
      (let [tick (.getInt buf)
            cmd-size (.get buf)]
        (recur
         (conj cmds (decode-command-block buf cmd-size tick))))
      cmds)))
          

;; The replay id is the first 4 bytes and is always 0x53526572
(defn unpack-replay-id
  [#^BinReplayUnpacker unpacker]
  (Integer/reverseBytes                 ; BIG_ENDIAN by default
   (.getInt
    (ByteBuffer/wrap
     (.unpackSection unpacker 4)))))

;; The headers are the first 0x279 bytes following the replay-id
(defn unpack-headers
  [#^BinReplayUnpacker unpacker]
  (.order (ByteBuffer/wrap (.unpackSection unpacker 0x279))
          ByteOrder/LITTLE_ENDIAN))

(defn unpack-commands
  [#^BinReplayUnpacker unpacker]
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
           :commands (decode-commands (unpack-commands unpacker))
           }]
    (.close unpacker) ; needs to be closed manually.
    m))
