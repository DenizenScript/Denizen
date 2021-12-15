package com.denizenscript.denizen.utilities;

import org.bukkit.Sound;

import java.util.HashMap;

public class SoundLookup {

    public static HashMap<String, Sound> keyToSound = new HashMap<>();

    static {
        for (Sound sound : Sound.values()) {
            keyToSound.put(sound.getKey().getKey(), sound);
        }
    }
}
