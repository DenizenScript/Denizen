package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

public class AssignmentScriptContainer extends ScriptContainer {

    public AssignmentScriptContainer(String scriptContainerName) {
        super(scriptContainerName);
    }

    public AssignmentScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }



}
