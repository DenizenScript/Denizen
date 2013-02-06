package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

public class EntityScriptContainer extends ScriptContainer {

    public EntityScriptContainer(String scriptContainerName) {
        super(scriptContainerName);
    }

    public EntityScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }


}
