package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

public class WorldScriptContainer extends ScriptContainer {

    public WorldScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        EventManager.world_scripts.put(getName(), this);
    }
}
