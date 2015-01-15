package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;

// <--[language]
// @name Assignment Script Containers
// @group Script Container System
// @description
// Assignment script-containers provide functionality to NPCs by 'assignment' of the container. Assignment
// scripts are meant to be used when customizing the normal behavior of NPCs. This can be used on a 'per-NPC' basis,
// but the encouraged approach is to design assignment scripts in a way that they can be used for multiple NPCs,
// perhaps with the use of constants or flags to determine specific information required by the scripts.

// Features unique to assignment script-containers include 'actions', 'constants', and 'interact script' assignment.
// Like any script, the ability to run local utility scripts can be accomplished as well. This allows fully
// interactive NPCs to be built purely with Assignment Scripts, and for advanced situations, world scripts and
// interact scripts can provide more functionality.

// Basic structure of an assignment script:
// <code>
// Assignment script container name:
//   type: assignment
//
//   actions:
//     on <action>:
//     - ...
//
//   default constants:
//     <constant_name>: <value>
//     <constant_list>:
//     - ...
//
//   interact scripts:
//   - <priority> <interact_script_name>
//   - ...
//   </code>
//
// Unlike other script containers, all part of an assignment script are optional. The three features provided can be
// used together, but do not require one another.

// -->

public class AssignmentScriptContainer extends ScriptContainer {

    public AssignmentScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
}
