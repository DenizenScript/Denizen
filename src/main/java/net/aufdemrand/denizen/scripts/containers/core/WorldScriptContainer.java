package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldScriptContainer extends ScriptContainer {

    public WorldScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        WorldScriptHelper.world_scripts.put(getName(), this);
    }




}
