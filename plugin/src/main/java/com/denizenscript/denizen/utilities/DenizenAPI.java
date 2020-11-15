package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.Denizen;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Provides some static methods for working with Denizen and Denizen-enabled NPCs
 */
public class DenizenAPI {

    private static Denizen denizen;

    /**
     * Gets the current instance of the Denizen plugin.
     *
     * @return Denizen instance
     */
    public static Denizen getCurrentInstance() {
        if (denizen == null) {
            denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
        }
        return denizen;
    }

    public static FileConfiguration getSaves() {
        return getCurrentInstance().getSaves();
    }
}
