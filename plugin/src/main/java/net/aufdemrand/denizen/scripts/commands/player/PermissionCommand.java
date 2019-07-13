package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

public class PermissionCommand extends AbstractCommand {

    // <--[command]
    // @Name Permission
    // @Syntax permission [add/remove] [permission] (group:<name>) (<world>)
    // @Required 2
    // @Short Gives or takes a permission node to/from the player or group.
    // @Group player
    // @Plugin Vault
    //
    // @Description
    // Adds or removes a permission node from a player or group. Accepts a world for world-based permissions
    // plugins. By default changes the attached player's permissions. Accepts the 'group:<name>' argument to change
    // a group's permission nodes rather than a player's.
    // Note: This requires a permissions plugin.
    //
    // @Tags
    // <p@player.has_permission[permission.node]>
    // <p@player.has_permission[permission.node].global>
    // <p@player.has_permission[permission.node].world[<world>]>
    // <server.has_permissions>
    //
    // @Usage
    // Use to give the player a permissions node.
    // - permission add bukkit.version
    //
    // @Usage
    // Use to remove a permissions node from a player.
    // - permission remove bukkit.version
    //
    // @Usage
    // Use to give the group 'Members' a permission node.
    // - permission add bukkit.version group:Members
    //
    // @Usage
    // Use to remove a permissions node from the group 'Members' in the Creative world.
    // - permission remove bukkit.version group:Members w@Creative
    // -->

    private enum Action {ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        if (Depends.permissions == null) {
            throw new InvalidArgumentsException("Permissions not linked - is Vault improperly installed, or is there no permissions plugin?");
        }

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("group")
                    && arg.matchesPrefix("group")) {
                scriptEntry.addObject("group", arg.asElement());
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", arg.asType(dWorld.class));
            }
            else if (!scriptEntry.hasObject("permission")) {
                scriptEntry.addObject("permission", arg.asElement());
            }

        }

        if (!scriptEntry.hasObject("group") && (!Utilities.entryHasPlayer(scriptEntry) || !Utilities.getEntryPlayer(scriptEntry).isValid())) {
            throw new InvalidArgumentsException("Must have player context or a valid group!");
        }

        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify a valid action!");
        }

        if (!scriptEntry.hasObject("permission")) {
            throw new InvalidArgumentsException("Must specify a permission!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element action = scriptEntry.getElement("action");
        Element permission = scriptEntry.getElement("permission");
        Element group = scriptEntry.getElement("group");
        dWorld world = (dWorld) scriptEntry.getObject("world");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), action.debug() + permission.debug()
                    + (group != null ? group.debug() : "") + (world != null ? world.debug() : ""));
        }

        World bukkitWorld = null;
        if (world != null) {
            bukkitWorld = world.getWorld();
        }

        OfflinePlayer player = Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getOfflinePlayer() : null;

        switch (Action.valueOf(action.asString().toUpperCase())) {
            case ADD:
                if (group != null) {
                    if (Depends.permissions.groupHas(bukkitWorld, group.asString(), permission.asString())) {
                        dB.echoDebug(scriptEntry, "Group " + group + " already has permission " + permission);
                    }
                    else {
                        Depends.permissions.groupAdd(bukkitWorld, group.asString(), permission.asString());
                    }
                }
                else {
                    if (Depends.permissions.playerHas(bukkitWorld == null ? null : bukkitWorld.getName(), player, permission.asString())) {
                        dB.echoDebug(scriptEntry, "Player " + player.getName() + " already has permission " + permission);
                    }
                    else {
                        Depends.permissions.playerAdd(bukkitWorld == null ? null : bukkitWorld.getName(), player, permission.asString());
                    }
                }
                return;
            case REMOVE:
                if (group != null) {
                    if (!Depends.permissions.groupHas(bukkitWorld, group.asString(), permission.asString())) {
                        dB.echoDebug(scriptEntry, "Group " + group + " does not have access to permission " + permission);
                    }
                    else {
                        Depends.permissions.groupRemove(bukkitWorld, group.asString(), permission.asString());
                    }
                }
                else {
                    if (!Depends.permissions.playerHas(bukkitWorld == null ? null : bukkitWorld.getName(), player, permission.asString())) {
                        dB.echoDebug(scriptEntry, "Player " + player.getName() + " does not have access to permission " + permission);
                    }
                    else {
                        Depends.permissions.playerRemove(bukkitWorld == null ? null : bukkitWorld.getName(), player, permission.asString());
                    }
                }
                return;
        }
    }
}
