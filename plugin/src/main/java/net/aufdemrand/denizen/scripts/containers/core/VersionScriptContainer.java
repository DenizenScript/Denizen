package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class VersionScriptContainer extends ScriptContainer {

    public static List<VersionScriptContainer> scripts = new ArrayList<VersionScriptContainer>();

    public VersionScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        scripts.add(this);
    }
}
