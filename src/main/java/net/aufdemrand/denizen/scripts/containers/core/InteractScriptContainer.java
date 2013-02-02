package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

public class InteractScriptContainer extends ScriptContainer {

    public InteractScriptContainer(String scriptContainerName) {
        super(scriptContainerName);
    }

    public InteractScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }


}
