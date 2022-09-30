package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.YamlConfiguration;

import java.util.List;

public class AssignmentScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Assignment Script Containers
    // @group Script Container System
    // @description
    // Assignment script-containers provide functionality to NPCs by 'assignment' of the container. Assignment
    // scripts are meant to be used when customizing the normal behavior of NPCs. This can be used on a 'per-NPC' basis,
    // but the encouraged approach is to design assignment scripts in a way that they can be used for multiple NPCs,
    // perhaps with the use of constants or flags to determine specific information required by the scripts.
    //
    // Features unique to assignment script-containers include 'actions' and 'interact script' assignment.
    // Like any script, the ability to run local utility scripts can be accomplished as well. This allows fully
    // interactive NPCs to be built purely with Assignment Scripts, and for advanced situations, world scripts and
    // interact scripts can provide more functionality.
    // See also <@link language interact script containers>
    //
    // Assignments scripts can be automatically disabled by adding "enabled: false" as a root key (supports any load-time-parseable tags).
    // This will disable any "actions" on the script (but not interact scripts steps - disable the interact for that).
    //
    // Basic structure of an assignment script:
    // <code>
    // Assignment_Script_Name:
    //
    //     type: assignment
    //
    //     # | All assignment scripts MUST have this key!
    //     actions:
    //         on <action>:
    //         - ...
    //
    //     # | Most assignment scripts should exclude this key, but it's available.
    //     default constants:
    //         <constant_name>: <value>
    //         <constant_list>:
    //         - ...
    //
    //     # | MOST assignment scripts should have this key!
    //     interact scripts:
    //     - <interact_script_name>
    // </code>
    //
    // Though note that almost always you should include the 'actions:' key, usually with the 'on assignment:' action (if using triggers).
    // Refer to <@link action assignment>.
    //
    // -->

    public AssignmentScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        if (contains("interact scripts", List.class)) {
            List<String> names = getStringList("interact scripts");
            if (!names.isEmpty()) {
                if (names.size() > 1) {
                    Debug.echoError("Assignment script '" + getName() + "' invalid: assignment scripts should only have ONE interact script in modern Denizen, not multiple!");
                }
                String name = names.get(0);
                int space = name.indexOf(' ');
                if (space != -1 && Character.isDigit(name.charAt(0))) {
                    BukkitImplDeprecations.interactScriptPriority.warn(this);
                    name = name.substring(space + 1).replace("^", "");
                }
                interactName = name;
            }
        }
    }

    public String interactName;

    public InteractScriptContainer interact;

    public InteractScriptContainer getInteract() {
        if (interact == null && interactName != null) {
            interact = ScriptRegistry.getScriptContainerAs(interactName, InteractScriptContainer.class);
            if (interact == null) {
                Debug.echoError("'" + interactName + "' is not a valid Interact Script. Is there a duplicate script by this name, or is it missing?");
            }
        }
        return interact;
    }
}
