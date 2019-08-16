package com.denizenscript.denizen.v1_12.helpers;

import com.denizenscript.denizen.nms.interfaces.SoundHelper;
import org.bukkit.Sound;

public class SoundHelperImpl implements SoundHelper {

    @Override
    public Sound getMidiInstrumentFromPatch(int patch) {
        // look up the instrument matching the patch
        switch (instruments_1_12[patch]) {
            case 0:
                return Sound.BLOCK_NOTE_HARP;
            case 1:
                return Sound.BLOCK_NOTE_BASS;
            case 2:
                return Sound.BLOCK_NOTE_SNARE;
            case 3:
                return Sound.BLOCK_NOTE_HAT;
            case 4:
                return Sound.BLOCK_NOTE_BASEDRUM;
            case 5:
                return Sound.BLOCK_NOTE_GUITAR;
            case 6:
                return Sound.BLOCK_NOTE_BELL;
            case 7:
                return Sound.BLOCK_NOTE_CHIME;
            case 8:
                return Sound.BLOCK_NOTE_FLUTE;
            case 9:
                return Sound.BLOCK_NOTE_XYLOPHONE;
            case 10:
                return Sound.BLOCK_NOTE_PLING;
        }
        return getDefaultMidiInstrument();
    }

    @Override
    public Sound getDefaultMidiInstrument() {
        return Sound.BLOCK_NOTE_HARP;
    }
}
