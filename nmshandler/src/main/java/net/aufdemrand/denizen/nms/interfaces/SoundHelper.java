package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.Sound;

public interface SoundHelper {

    // provided by github.com/sk89q/craftbook
    int[] instruments = {
            0, 0, 0, 0, 0, 0, 0, 5, //   8
            6, 0, 0, 0, 0, 0, 0, 0, //  16
            0, 0, 0, 0, 0, 0, 0, 5, //  24
            5, 5, 5, 5, 5, 5, 5, 5, //  32
            6, 6, 6, 6, 6, 6, 6, 6, //  40
            5, 5, 5, 5, 5, 5, 5, 2, //  48
            5, 5, 5, 5, 0, 0, 0, 0, //  56
            0, 0, 0, 0, 0, 0, 0, 0, //  64
            0, 0, 0, 0, 0, 0, 0, 0, //  72
            0, 0, 0, 0, 0, 0, 0, 0, //  80
            0, 0, 0, 0, 0, 0, 0, 0, //  88
            0, 0, 0, 0, 0, 0, 0, 0, //  96
            0, 0, 0, 0, 0, 0, 0, 0, // 104
            0, 0, 0, 0, 0, 0, 0, 0, // 112
            1, 1, 1, 3, 1, 1, 1, 5, // 120
            1, 1, 1, 1, 1, 2, 4, 3, // 128
    };


    Sound getMidiInstrumentFromPatch(int patch);

    Sound getDefaultMidiInstrument();

    Sound getChestOpen();

    Sound getChestClose();
}
