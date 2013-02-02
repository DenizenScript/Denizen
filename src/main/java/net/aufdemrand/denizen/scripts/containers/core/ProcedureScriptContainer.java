package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.ScriptContainer;
import org.bukkit.configuration.ConfigurationSection;

public class ProcedureScriptContainer extends ScriptContainer {

    public ProcedureScriptContainer(String scriptContainerName) {
        super(scriptContainerName);
    }

    public ProcedureScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

}
