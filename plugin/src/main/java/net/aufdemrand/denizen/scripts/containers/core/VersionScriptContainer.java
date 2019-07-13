package net.aufdemrand.denizen.scripts.containers.core;

import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class VersionScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Version Script Containers
    // @group Script Container System
    // @description
    // Version script containers are used to identify a public script's version, author, and etc. basic information.
    //
    // These are primarily for use with the public script repository, and as such generally contain
    // an 'ID:' key identifying the script repo ID of the script.
    //
    // <code>
    // Version_Script_Name:
    //
    //   type: version
    //
    //   # This is sample information, for script repository script number zero, 'dSentry'.
    //   id: 0
    //   version: 0.7.3
    //   name: dSentry
    //   author: mcmonkey
    //
    // </code>
    //
    // -->

    public static List<VersionScriptContainer> scripts = new ArrayList<>();

    public VersionScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        scripts.add(this);
    }
}
