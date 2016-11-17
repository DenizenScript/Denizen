package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.SoundHelper;
import org.bukkit.Sound;

public class SoundHelper_v1_11_R1 implements SoundHelper {

    @Override
    public Sound getMidiInstrumentFromPatch(int patch) {
        // look up the instrument matching the patch
        switch (instruments[patch]) {
            case 1:
                return Sound.BLOCK_NOTE_BASS;
            case 2:
                return Sound.BLOCK_NOTE_SNARE;
            case 3:
                return Sound.BLOCK_NOTE_HARP;
            case 4:
                return Sound.BLOCK_NOTE_HAT;
            case 5:
                return Sound.BLOCK_NOTE_PLING;
            case 6:
                return Sound.BLOCK_NOTE_BASEDRUM;
        }
        return getDefaultMidiInstrument();
    }

    @Override
    public Sound getDefaultMidiInstrument() {
        return Sound.BLOCK_NOTE_HAT;
    }

    @Override
    public Sound getChestOpen() {
        return Sound.BLOCK_CHEST_OPEN;
    }

    @Override
    public Sound getChestClose() {
        return Sound.BLOCK_CHEST_CLOSE;
    }
}
