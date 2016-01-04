package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

public class PermissionCommand extends AbstractCommand {

    private enum Action {ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

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

        if (!scriptEntry.hasObject("group") && (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() || !((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().isValid())) {
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
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element action = scriptEntry.getElement("action");
        Element permission = scriptEntry.getElement("permission");
        Element group = scriptEntry.getElement("group");
        dWorld world = (dWorld) scriptEntry.getObject("world");

        // Report to dB
        dB.report(scriptEntry, getName(), action.debug() + permission.debug()
                + (group != null ? group.debug() : "") + (world != null ? world.debug() : ""));

        World bukkitWorld = null;
        if (world != null) {
            bukkitWorld = world.getWorld();
        }

        OfflinePlayer player = ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getOfflinePlayer() : null;

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
