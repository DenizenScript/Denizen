package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;

public class WorldScriptContainer extends ScriptContainer {

    public WorldScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        EventManager.world_scripts.put(getName(), this);
    }
}
