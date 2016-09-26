package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.SoundHelper;
import org.bukkit.Sound;

public class SoundHelper_v1_8_R3 implements SoundHelper {

    @Override
    public Sound getMidiInstrumentFromPatch(int patch) {
        // look up the instrument matching the patch
        switch (instruments[patch]) {
            case 1:
                return Sound.NOTE_BASS_GUITAR;
            case 2:
                return Sound.NOTE_SNARE_DRUM;
            case 3:
                return Sound.NOTE_STICKS;
            case 4:
                return Sound.NOTE_BASS_DRUM;
            case 5:
                return Sound.NOTE_PLING;
            case 6:
                return Sound.NOTE_BASS;
        }
        return getDefaultMidiInstrument();
    }

    @Override
    public Sound getDefaultMidiInstrument() {
        return Sound.NOTE_PIANO;
    }

    @Override
    public Sound getChestOpen() {
        return Sound.CHEST_OPEN;
    }

    @Override
    public Sound getChestClose() {
        return Sound.CHEST_CLOSE;
    }
}
