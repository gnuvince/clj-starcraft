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
  [:unknown 2 Byte])

(defaction ally
  "Ally"
  [:unknown 4 Byte])

(defaction hot-key
  "Hot key"
  [:action 1 Byte #(["Set" "Get"] (int %))]
  [:number 1 Byte])

(defaction move
  "Move"
  [:pos-x 1 Short]
  [:pos-y 1 Short]
  [:unit-id 1 Short]
  [:unknown1 1 Short]
  [:unknown2 1 Byte])

(defaction attack
  "Attack/Right Click/Cast Magic/Use Ability"
  [:pos-x 1 Short]
  [:pos-y 1 Short]
  [:unit-id 1 Short]
  [:unknown 1 Short]
  [:action 1 Byte #(*attacks* (int %))]
  [:shifted 1 Byte])

(defaction cancel
  "Cancel")

(defaction cancel-hatch
  "Cancel Hatch")

(defaction stop
  "Stop"
  [:unknown 1 Byte])

(defaction return-cargo
  "Return Cargo"
  [:unknown 1 Byte])

(defaction train
  "Train"
  [:unit-type 1 Short #(*units* (int %))])

(defaction cancel-train
  "Cancel train"
  [:unknown 2 Byte])

(defaction cloak
  "Cloak"
  [:unknown 1 Byte])

(defaction decloak
  "Decloak"
  [:unknown 1 Byte])

(defaction hatch
  "Hatch"
  [:unit-type 1 Short #(*units* (int %))])

(defaction unsiege
  "Unsiege"
  [:unknown 1 Byte])

(defaction siege
  "Siege"
  [:unknown 1 Byte])

(defaction build-interceptor
  "Build Interceptor/Scarab")

(defaction unload-all
  "Unload All"
  [:unknown 1 Byte])

(defaction unload
  "Unload"
  [:unknown 2 Byte])

(defaction merge-archon
  "Merge Archon")

(defaction hold-position
  "Hold Position"
  [:unknown 1 Byte])

(defaction burrow
  "Burrow"
  [:unknown 1 Byte])

(defaction unburrow
  "Unburrow"
  [:unknown 1 Byte])

(defaction cancel-nuke
  "Cancel Nuke")

(defaction lift
  "Lift"
  [:unknown 4 Byte])

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
      0x35 morph
      0x36 stim
      0x57 leave-game
      0x5A merge-dark-archon
      })
            
