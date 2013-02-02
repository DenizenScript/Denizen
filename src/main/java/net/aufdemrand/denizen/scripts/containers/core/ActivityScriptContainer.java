package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

public class ActivityScriptContainer extends ScriptContainer {

    public ActivityScriptContainer(String scriptContainerName) {
        super(scriptContainerName);
    }

    public ActivityScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
}
