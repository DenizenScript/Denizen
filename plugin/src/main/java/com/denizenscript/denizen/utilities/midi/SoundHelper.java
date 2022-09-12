package com.denizenscript.denizen.utilities.midi;

import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundHelper {

    private static final int[] instruments_1_12 = {
            0, 0, 0, 0, 0, 0, 0, 5, // 8
            9, 9, 9, 9, 9, 6, 0, 9, // 16
            9, 0, 0, 0, 0, 0, 0, 5, // 24
            5, 5, 5, 5, 5, 5, 5, 1, // 32
            1, 1, 1, 1, 1, 1, 1, 5, // 40
            1, 5, 5, 5, 5, 5, 5, 5, // 48
            5, 5, 5, 8, 8, 8, 8, 8, // 56
            8, 8, 8, 8, 8, 8, 8, 8, // 64
            8, 8, 8, 8, 8, 8, 8, 8, // 72
            8, 8, 8, 8, 8, 8, 8, 8, // 80
            0, 0, 0, 0, 0, 0, 0, 0, // 88
            0, 0, 0, 0, 0, 0, 0, 0, // 96
            0, 0, 0, 0, 0, 0, 0, 5, // 104
            5, 5, 5, 9, 8, 5, 8, 6, // 112
            6, 3, 3, 2, 2, 2, 6, 5, // 120
            1, 1, 1, 6, 1, 2, 4, 7, // 128
    };

    public static Sound getMidiInstrumentFromPatch(int patch) {
        switch (instruments_1_12[patch]) {
            case 0: return Sound.BLOCK_NOTE_BLOCK_HARP;
            case 1: return Sound.BLOCK_NOTE_BLOCK_BASS;
            case 2: return Sound.BLOCK_NOTE_BLOCK_SNARE;
            case 3: return Sound.BLOCK_NOTE_BLOCK_HAT;
            case 4: return Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case 5: return Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case 6: return Sound.BLOCK_NOTE_BLOCK_BELL;
            case 7: return Sound.BLOCK_NOTE_BLOCK_CHIME;
            case 8: return Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case 9: return Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case 10: return Sound.BLOCK_NOTE_BLOCK_PLING;
            default: return defaultMidiInstrument;
        }
    }

    public static final Sound defaultMidiInstrument = Sound.BLOCK_NOTE_BLOCK_HARP;

    public static void playSound(Player player, Location location, String sound, float volume, float pitch, String category) {
        SoundCategory categoryEnum = category != null ? new ElementTag(category).asEnum(SoundCategory.class) : null;
        if (categoryEnum == null) {
            categoryEnum = SoundCategory.MASTER;
        }
        if (player == null) {
            location.getWorld().playSound(location, sound, categoryEnum, volume, pitch);
        }
        else {
            player.playSound(location, sound, categoryEnum, volume, pitch);
        }
    }

    public static void playSound(Player player, Location location, Sound sound, float volume, float pitch, String category) {
        SoundCategory categoryEnum = category != null ? new ElementTag(category).asEnum(SoundCategory.class) : null;
        if (categoryEnum == null) {
            categoryEnum = SoundCategory.MASTER;
        }
        if (player == null) {
            location.getWorld().playSound(location, sound, categoryEnum, volume, pitch);
        }
        else {
            player.playSound(location, sound, categoryEnum, volume, pitch);
        }
    }
}
