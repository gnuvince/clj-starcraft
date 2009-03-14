(ns starcraft.replay.actions
  (:use [starcraft.replay.parse :as parse]
        [starcraft.replay.action-maps]))

(defn action
  [name & v-forms]
  {:name name
   :fields v-forms})

(def *actions*
     {0x09 (action "Select Units"
                   [:units-id [1 Byte] Short])
      0x0A (action "Shift Select Units"
                   [:units-id [1 Byte] Short])
      0x0B (action "Shift Deselect Units"
                   [:units-id [1 Byte] Short])
      0x0C (action "Build"
                   [:unit 1 Byte #(*units* (int %))]
                   [:pos-x 1 Short]
                   [:pos-y 1 Short]
                   [:unit-id 1 Short])
      0x0D (action "Vision"
                   [nil 2 Byte])
      0x0E (action "Ally"
                   [nil 4 Byte])
      0x13 (action "Hot key"
                   [:action 1 Byte #(get ["Set" "Get"] (int %) %)]
                   [:number 1 Byte])
      0x14 (action "Move"
                   [:pos-x 1 Short]
                   [:pos-y 1 Short]
                   [:unit-id 1 Short]
                   [nil 1 Short]
                   [nil 1 Byte])
      0x15 (action "Attack/Right Click/Cast Magic/Use Ability"
                   [:pos-x 1 Short]
                   [:pos-y 1 Short]
                   [:unit-id 1 Short]
                   [nil 1 Short]
                   [:action 1 Byte #(*attacks* (int %))]
                   [:shifted 1 Byte])
      0x18 (action "Cancel")
      0x19 (action "Cancel Hatch")
      0x1A (action "Stop"
                   [nil 1 Byte])
      0x1B (action "Carrier Stop")
      0x1C (action "Reaver Stop")
      0x1E (action "Return Cargo"
                   [nil 1 Byte])
      0x1F (action "Train"
                   [:unit-type 1 Short #(*units* (int %))])
      0x20 (action "Cancel train"
                   [nil 2 Byte])
      0x21 (action "Cloak"
                   [nil 1 Byte])
      0x22 (action "Decloak"
                   [nil 1 Byte])
      0x23 (action "Hatch"
                   [:unit-type 1 Short #(*units* (int %))])
      0x25 (action "Unsiege"
                   [nil 1 Byte])
      0x26 (action "Siege"
                   [nil 1 Byte])
      0x27 (action "Build Interceptor/Scarab")
      0x28 (action "Unload All"
                   [nil 1 Byte])
      0x29 (action "Unload"
                   [nil 2 Byte])
      0x2A (action "Merge Archon")
      0x2B (action "Hold Position"
                   [nil 1 Byte])
      0x2C (action "Burrow"
                   [nil 1 Byte])
      0x2D (action "Unburrow"
                   [nil 1 Byte])
      0x2E (action "Cancel Nuke")
      0x2F (action "Lift"
                   [nil 4 Byte])
      0x30 (action "Research"
                   [:research 1 Byte #(*researches* (int %))])
      0x31 (action "Cancel Research")
      0x32 (action "Upgrade"
                   [:upgrade 1 Byte #(*upgrades* (int %))])
      0x33 (action "Cancel upgrade")
      0x34 (action "Cancel add-on")
      0x35 (action "Morph"
                   [:building 1 Short #(*units* (int %))])
      0x36 (action "Stim")
      0x57 (action "Leave Game"
                   [:reason 1 Byte #({1 "Quit", 6 "Drop"} (int %))])
      0x58 (action "Minimap ping"
                   [:pos-x 1 Short]
                   [:pos-y 1 Short])
      0x5A (action "Merge Dark Archon")
      0x5C (action "Game chat"
                   [:player-id 1 Byte]
                   [:message 80 String])
      })
