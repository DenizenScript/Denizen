package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

public class GroupCommand extends AbstractCommand {

    public GroupCommand() {
        setName("group");
        setSyntax("group [add/remove/set] [<group>] (<world>)");
        setRequiredArguments(2, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Group
    // @Syntax group [add/remove/set] [<group>] (<world>)
    // @Required 2
    // @Maximum 3
    // @Short Adds a player to, removes a player from, or sets a player's permissions group.
    // @Group player
    // @Plugin Vault
    //
    // @Description
    // Controls a player's permission groups, which the ability to add, remove or set a player's groups.
    // The 'add' argument adds the player to the group and any parent groups,
    // and the 'remove' command does the opposite, removing the player from the group and any inheriting groups.
    // The set command removes all existing groups and sets the player's group.
    // Note: This requires a permissions plugin and Vault.
    //
    // @Tags
    // <PlayerTag.in_group[<group>]>
    // <PlayerTag.in_group[<group>].global>
    // <PlayerTag.in_group[<group>].world>
    // <PlayerTag.groups[(<world>)]>
    // <server.permission_groups>
    //
    // @Usage
    // Use to add a player to the Admin group.
    // - group add Admin
    //
    // @Usage
    // Use to remove a player from the Moderator group.
    // - group remove Moderator
    //
    // @Usage
    // Use to set a player to the Member group in the Creative world.
    // - group set Member Creative
    // -->

    private enum Action {ADD, REMOVE, SET}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        if (Depends.permissions == null) {
            throw new InvalidArgumentsException("Permissions not linked - is Vault improperly installed, or is there no permissions plugin?");
        }
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.class)) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("world", arg.asType(WorldTag.class));
            }
            else if (!scriptEntry.hasObject("group")) {
                scriptEntry.addObject("group", arg.asElement());
            }
        }
        if (!Utilities.entryHasPlayer(scriptEntry) || !Utilities.getEntryPlayer(scriptEntry).isValid()) {
            throw new InvalidArgumentsException("Must have player context!");
        }
        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify valid action!");
        }
        if (!scriptEntry.hasObject("group")) {
            throw new InvalidArgumentsException("Must specify a group name!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag action = scriptEntry.getElement("action");
        WorldTag world = scriptEntry.getObjectTag("world");
        ElementTag group = scriptEntry.getElement("group");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), action, world, group);
        }
        World bukkitWorld = null;
        if (world != null) {
            bukkitWorld = world.getWorld();
        }
        OfflinePlayer player = Utilities.getEntryPlayer(scriptEntry).getOfflinePlayer();
        boolean inGroup = Depends.permissions.playerInGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, group.asString());
        switch (Action.valueOf(action.asString().toUpperCase())) {
            case ADD:
                if (inGroup) {
                    Debug.echoDebug(scriptEntry, "Player " + player.getName() + " is already in group " + group);
                }
                else {
                    Depends.permissions.playerAddGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, group.asString());
                }
                break;
            case REMOVE:
                if (!inGroup) {
                    Debug.echoDebug(scriptEntry, "Player " + player.getName() + " is not in group " + group);
                }
                else {
                    Depends.permissions.playerRemoveGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, group.asString());
                }
                break;
            case SET:
                for (String grp : Depends.permissions.getPlayerGroups((bukkitWorld == null ? null : bukkitWorld.getName()), player)) {
                    Depends.permissions.playerRemoveGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, grp);
                }
                Depends.permissions.playerAddGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, group.asString());
                break;
        }
    }
}
