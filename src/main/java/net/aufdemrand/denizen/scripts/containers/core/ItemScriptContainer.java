package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

public class ItemScriptContainer extends ScriptContainer {

    public ItemScriptContainer(String scriptContainerName) {
        super(scriptContainerName);
    }

    public ItemScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

}
