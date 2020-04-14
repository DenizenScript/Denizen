package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.YamlConfiguration;

public class VersionScriptContainer extends ScriptContainer {

    public VersionScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        canRunScripts = false;
        Deprecations.versionScripts.warn(this);
    }
}
