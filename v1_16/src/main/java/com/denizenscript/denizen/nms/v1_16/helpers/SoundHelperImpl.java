package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.nms.interfaces.SoundHelper;
import org.bukkit.Sound;

public class SoundHelperImpl implements SoundHelper {

    @Override
    public Sound getMidiInstrumentFromPatch(int patch) {
        // look up the instrument matching the patch
        // TODO: 1.14 - does this still work? (last verified in 1.12)
        switch (instruments_1_12[patch]) {
            case 0:
                return Sound.BLOCK_NOTE_BLOCK_HARP;
            case 1:
                return Sound.BLOCK_NOTE_BLOCK_BASS;
            case 2:
                return Sound.BLOCK_NOTE_BLOCK_SNARE;
            case 3:
                return Sound.BLOCK_NOTE_BLOCK_HAT;
            case 4:
                return Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case 5:
                return Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case 6:
                return Sound.BLOCK_NOTE_BLOCK_BELL;
            case 7:
                return Sound.BLOCK_NOTE_BLOCK_CHIME;
            case 8:
                return Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case 9:
                return Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case 10:
                return Sound.BLOCK_NOTE_BLOCK_PLING;
        }
        return getDefaultMidiInstrument();
    }
}
