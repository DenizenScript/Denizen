package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

public class TaskScriptContainer extends ScriptContainer {


    public TaskScriptContainer(String scriptContainerName) {
        super(scriptContainerName);
    }

    public TaskScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
}
