(ns starcraft.replay.unpack
  (:use [starcraft.replay.parse]
        [starcraft.replay.actions])
  (:import [hu.belicza.andras.bwhf.control BinReplayUnpacker]
           [java.io File]
           [java.nio ByteBuffer ByteOrder]
           [java.util Date]))

(defn decode-player-data
  "Decode a single player's data into a map."
  [#^ByteBuffer buf]
  (assoc (parse-buffer buf
            [:player-number  1 Integer]
            [:slot-number    1 Integer]
            [:type           1 Byte #({0 nil, 1 :cpu, 2 :human} %)]
            [:race           1 Byte #({0 "Zerg"
                                       1 "Terran"
                                       2 "Protoss"
                                       6 "Random"} %)]
            [nil             1 Byte]
            [:name          25 String])
    :actions []))

(defn decode-players-data
  [data]
  (let [players (partition 36 data)]
    (vec (map (fn [vec]
                (let [buf (ByteBuffer/wrap (into-array Byte/TYPE (map byte vec)))]
                  (.order buf ByteOrder/LITTLE_ENDIAN)
                  (decode-player-data buf)))
              players))))


(defn decode-headers
  [#^ByteBuffer buf]
  (parse-buffer buf
    [:game-engine         1 Byte]
    [:game-frames         1 Integer]
    [nil                  3 Byte]
    [:save-time           1 Integer #(Date. (long (* 1000 %)))]
    [:slots-available    12 Byte]
    [:game-name          28 String]
    [:map-width           1 Short]
    [:map-height          1 Short]
    [:is-broodwar2        1 Byte]
    [:available-slots     1 Byte]
    [:speed               1 Short]
    [:type                1 Short]
    [:subtype             1 Short]
    [:rand-seed           1 Integer]
    [:tile-set            1 Short]
    [nil                  1 Short]
    [:creator-name       25 String]
    [:map-name           32 String]
    [:type2               1 Short]
    [:subtype2            1 Short]
    [:subtype-display     1 Short]
    [:subtype-label       1 Short]
    [:victory-conditions  1 Byte {0x00 "Map Default"
                                  0x01 "Melee"
                                  0x02 "Highest Score"
                                  0x03 "Resources"
                                  0x04 "CTF"
                                  0x05 "Sudden Death"
                                  0x06 "Slaughter"
                                  0x07 "One on One"}]
    [:resource-type       1 Byte {0x00 "Map Default"
                                  0x01 "Fixed Value"
                                  0x02 "Low"
                                  0x03 "Medium"
                                  0x04 "High"
                                  0x05 "Income"}]
    [:standard-unit-stats 1 Byte]
    [:fog-of-war-unused   1 Byte]
    [:starting-units      1 Byte {0x00 "Map Default"
                                  0x01 "Workers Only"
                                  0x02 "Workers and Center"}]
    [:use-fixed-positions 1 Byte]
    [:restriction-flags   1 Byte {0x01 "Allow Computer Players"
                                  0x02 "Allow Single Player"}]
    [:allies-allowed      1 Byte]
    [:teams               1 Byte]
    [:cheats              1 Byte]
    [:tournament-mode     1 Byte]
    [:victory-condition-value 1 Integer]
    [:resources-value     1 Integer]
    [nil                  4 Byte]
    [:players           432 Byte decode-players-data]
    [:player-spot-color   8 Integer]
    [:player-spot-index   8 Byte]))

;; FIXME: refactor this function
(defn decode-command-block
  [#^ByteBuffer buf cmd-size tick cmds]
  (let [end (+ cmd-size (.position buf))]
    (loop [v cmds]
      (if (= (.position buf) end)
        v
        (let [{:keys [player-id action-id]} (parse-buffer buf
                                              [:player-id 1 Byte]
                                              [:action-id 1 Byte])
              {:keys [name fields]} (*actions* (int action-id))
              action (apply parse-buffer buf fields)]
          (if action
            (recur (update-in v
                              [(int player-id)]
                              conj
                              (merge {:tick tick
                                      :name name}
                                     action)))
            ;; Move to the end of the command block and return v
            ;; if the action-id is unknown.
            (do
              (.position buf end)
              v)))))))


(defn decode-commands
  [#^ByteBuffer buf]
  (loop [cmds (vec (replicate 12 []))]
    (if (.hasRemaining buf)
      (let [{:keys [tick cmd-size]} (parse-buffer buf
                                                  [:tick 1 Integer]
                                                  [:cmd-size 1 Byte])]
        (recur (decode-command-block buf cmd-size tick cmds)))
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
        replay-id (unpack-replay-id unpacker)
        -headers (decode-headers (unpack-headers unpacker))
        -players (:players -headers)
        headers (dissoc -headers :players)
        commands (decode-commands (unpack-commands unpacker))
        players (vec (map #(assoc %1 :actions %2) -players commands))]
    (.close unpacker) ; needs to be closed manually.
    {:replay-id replay-id
     :headers headers
     :players players
     }))
