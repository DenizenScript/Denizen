package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

public class BookScriptContainer extends ScriptContainer {

    public BookScriptContainer(String scriptContainerName) {
        super(scriptContainerName);
    }

    public BookScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
}
