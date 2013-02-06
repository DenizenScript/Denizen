package net.aufdemrand.denizen.scripts.containers;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

public class ScriptContainer extends MemorySection {



    public ScriptContainer(String scriptContainerName) {
        super(DenizenAPI.getCurrentInstance().getScripts(), scriptContainerName);
        if (getType() == null) throw new IllegalStateException("Could not locate Script.");
    }

    public ScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        if (getType() == null) throw new IllegalStateException("Could not locate Script.");
    }

    public String getType() {
        return getString("TYPE");
    }

}
