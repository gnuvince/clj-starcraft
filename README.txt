javac hu/belicza/andras/bwhf/control/BinReplayUnpacker.java

(use '[starcraft.replay.unpack])
(import '[java.io File])
(count (unpack (File. "replay.rep")))   ; Use count, because the output of
                                        ; unpack is very long
