package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.YamlConfiguration;

public class VersionScriptContainer extends ScriptContainer {

    public VersionScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        canRunScripts = false;
        BukkitImplDeprecations.versionScripts.warn(this);
    }
}
