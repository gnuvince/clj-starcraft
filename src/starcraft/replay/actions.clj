(ns starcraft.replay.actions
  (:use [starcraft.replay.parse :as parse]
        [starcraft.replay.action-maps]))

(defmacro defaction
  [name str-name & v-forms]
  `(def ~name
        {:name ~str-name
         :fields [~@v-forms]}))

(defaction select-units
  "Select Units"
  [:units-id [1 Byte] Short])

(defaction shift-select-units
  "Shift Select Units"
  [:units-id [1 Byte] Short])

(defaction shift-deselect-units
  "Shift Deselect Units"
  [:units-id [1 Byte] Short])

(defaction build
  "Build"
  [:unit 1 Byte #(*units* (int %))]
  [:pos-x 1 Short]
  [:pos-y 1 Short]
  [:unit-id 1 Short])

(defaction vision
  "Vision"
  [nil 2 Byte])

(defaction ally
  "Ally"
  [nil 4 Byte])

(defaction hot-key
  "Hot key"
  [:action 1 Byte #(get ["Set" "Get"] (int %) %)]
  [:number 1 Byte])

(defaction move
  "Move"
  [:pos-x 1 Short]
  [:pos-y 1 Short]
  [:unit-id 1 Short]
  [nil 1 Short]
  [nil 1 Byte])

(defaction attack
  "Attack/Right Click/Cast Magic/Use Ability"
  [:pos-x 1 Short]
  [:pos-y 1 Short]
  [:unit-id 1 Short]
  [nil 1 Short]
  [:action 1 Byte #(*attacks* (int %))]
  [:shifted 1 Byte])

(defaction cancel
  "Cancel")

(defaction cancel-hatch
  "Cancel Hatch")

(defaction stop
  "Stop"
  [nil 1 Byte])

(defaction return-cargo
  "Return Cargo"
  [nil 1 Byte])

(defaction train
  "Train"
  [:unit-type 1 Short #(*units* (int %))])

(defaction cancel-train
  "Cancel train"
  [nil 2 Byte])

(defaction cloak
  "Cloak"
  [nil 1 Byte])

(defaction decloak
  "Decloak"
  [nil 1 Byte])

(defaction hatch
  "Hatch"
  [:unit-type 1 Short #(*units* (int %))])

(defaction unsiege
  "Unsiege"
  [nil 1 Byte])

(defaction siege
  "Siege"
  [nil 1 Byte])

(defaction build-interceptor
  "Build Interceptor/Scarab")

(defaction unload-all
  "Unload All"
  [nil 1 Byte])

(defaction unload
  "Unload"
  [nil 2 Byte])

(defaction merge-archon
  "Merge Archon")

(defaction hold-position
  "Hold Position"
  [nil 1 Byte])

(defaction burrow
  "Burrow"
  [nil 1 Byte])

(defaction unburrow
  "Unburrow"
  [nil 1 Byte])

(defaction cancel-nuke
  "Cancel Nuke")

(defaction lift
  "Lift"
  [nil 4 Byte])

(defaction research
  "Research"
  [:research 1 Byte #(*researches* (int %))])

(defaction cancel-research
  "Cancel Research")

(defaction upgrade
  "Upgrade"
  [:upgrade 1 Byte #(*upgrades* (int %))])

(defaction morph
  "Morph"
  [:building 1 Short #(*units* (int %))])

(defaction stim
  "Stim")

(defaction leave-game
  "Leave Game"
  [:reason 1 Byte #({1 "Quit", 6 "Drop"} (int %))])

(defaction merge-dark-archon
  "Merge Dark Archon")

(defaction reaver-stop
  "Reaver Stop")

(defaction carrier-stop
  "Carrier Stop")

(defaction cancel-upgrade
  "Cancel upgrade")

(defaction cancel-addon
  "Cancel add-on")

(defaction minimap-ping
  "Minimap ping"
  [:pos-x 1 Short]
  [:pos-y 1 Short])

(defaction game-chat
  "Game chat"
  [:player-id 1 Byte]
  [:message 80 String])

(def *actions*
     {0x09 select-units
      0x0A shift-select-units
      0x0B shift-deselect-units
      0x0C build
      0x0D vision
      0x0E ally
      0x13 hot-key
      0x14 move
      0x15 attack
      0x18 cancel
      0x19 cancel-hatch
      0x1A stop
      0x1B carrier-stop
      0x1C reaver-stop
      0x1E return-cargo
      0x1F train
      0x20 cancel-train
      0x21 cloak
      0x22 decloak
      0x23 hatch
      0x25 unsiege
      0x26 siege
      0x27 build-interceptor
      0x28 unload-all
      0x29 unload
      0x2A merge-archon
      0x2B hold-position
      0x2C burrow
      0x2D unburrow
      0x2E cancel-nuke
      0x2F lift
      0x30 research
      0x31 cancel-research
      0x32 upgrade
      0x33 cancel-upgrade
      0x34 cancel-addon
      0x35 morph
      0x36 stim
      0x57 leave-game
      0x58 minimap-ping
      0x5A merge-dark-archon
      0x5C game-chat
      })
            