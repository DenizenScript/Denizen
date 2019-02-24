package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

public class GroupCommand extends AbstractCommand {

    private enum Action {ADD, REMOVE, SET}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        if (Depends.permissions == null) {
            throw new InvalidArgumentsException("Permissions not linked - is Vault improperly installed, or is there no permissions plugin?");
        }

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", arg.asType(dWorld.class));
            }
            else if (!scriptEntry.hasObject("group")) {
                scriptEntry.addObject("group", arg.asElement());
            }

        }

        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() || !((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().isValid()) {
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

        Element action = scriptEntry.getElement("action");
        dWorld world = (dWorld) scriptEntry.getObject("world");
        Element group = scriptEntry.getElement("group");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), action.debug() + (world != null ? world.debug() : "") + group.debug());
        }

        World bukkitWorld = null;
        if (world != null) {
            bukkitWorld = world.getWorld();
        }

        OfflinePlayer player = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getOfflinePlayer();
        boolean inGroup = Depends.permissions.playerInGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, group.asString());

        switch (Action.valueOf(action.asString().toUpperCase())) {
            case ADD:
                if (inGroup) {
                    dB.echoDebug(scriptEntry, "Player " + player.getName() + " is already in group " + group);
                }
                else {
                    Depends.permissions.playerAddGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, group.asString());
                }
                return;
            case REMOVE:
                if (!inGroup) {
                    dB.echoDebug(scriptEntry, "Player " + player.getName() + " is not in group " + group);
                }
                else {
                    Depends.permissions.playerRemoveGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, group.asString());
                }
                return;
            case SET:
                for (String grp : Depends.permissions.getPlayerGroups((bukkitWorld == null ? null : bukkitWorld.getName()), player)) {
                    Depends.permissions.playerRemoveGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, grp);
                }
                Depends.permissions.playerAddGroup((bukkitWorld == null ? null : bukkitWorld.getName()), player, group.asString());
        }
    }
}
