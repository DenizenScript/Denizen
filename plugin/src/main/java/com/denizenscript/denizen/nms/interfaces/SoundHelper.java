package com.denizenscript.denizen.nms.interfaces;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public interface SoundHelper {

    int[] instruments_1_12 = {
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

    Sound getMidiInstrumentFromPatch(int patch);

    Sound getDefaultMidiInstrument();

    default void playSound(Player player, Location location, String sound, float volume, float pitch, String category) {
        if (player == null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
        else {
            player.playSound(location, sound, volume, pitch);
        }
    }

    default void playSound(Player player, Location location, Sound sound, float volume, float pitch, String category) {
        if (player == null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
        else {
            player.playSound(location, sound, volume, pitch);
        }
    }
}
