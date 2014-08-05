package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import org.bukkit.OfflinePlayer;


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
                scriptEntry.addObject("world", new Element(arg.asType(dWorld.class).getName()));
            }

            else if (!scriptEntry.hasObject("permission")) {
                scriptEntry.addObject("permission", arg.asElement());
            }

        }

        if (!scriptEntry.hasObject("group") && (!scriptEntry.hasPlayer() || !scriptEntry.getPlayer().isValid()))
            throw new InvalidArgumentsException("Must have player context or a valid group!");

        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify a valid action!");

        if (!scriptEntry.hasObject("permission"))
            throw new InvalidArgumentsException("Must specify a permission!");

        scriptEntry.defaultObject("world", Element.NULL);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element action = scriptEntry.getElement("action");
        Element permission = scriptEntry.getElement("permission");
        Element group = scriptEntry.getElement("group");
        Element world = scriptEntry.getElement("world");

        // Report to dB
        dB.report(scriptEntry, getName(), action.debug() + permission.debug()
                + (group != null ? group.debug() : "") + world.debug());

        OfflinePlayer player = scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getOfflinePlayer() : null;

        switch (Action.valueOf(action.asString().toUpperCase())) {
            case ADD:
                if (group != null) {
                    if (Depends.permissions.groupHas(world.asString(), group.asString(), permission.asString()))
                        dB.echoDebug(scriptEntry, "Group " + group + " already has permission " + permission);
                    else
                        Depends.permissions.groupAdd(world.asString(), group.asString(), permission.asString());
                } else {
                    if(Depends.permissions.playerHas(world.asString(), player, permission.asString()))
                        dB.echoDebug(scriptEntry, "Player " + player.getName() + " already has permission " + permission);
                    else
                        Depends.permissions.playerAdd(world.asString(), player, permission.asString());
                }
                return;
            case REMOVE:
                if (group != null) {
                    if(!Depends.permissions.groupHas(world.asString(), group.asString(), permission.asString()))
                        dB.echoDebug(scriptEntry, "Group " + group + " does not have access to permission " + permission);
                    else
                        Depends.permissions.groupRemove(world.asString(), group.asString(), permission.asString());
                } else {
                    if(!Depends.permissions.playerHas(world.asString(), player, permission.asString()))
                        dB.echoDebug(scriptEntry, "Player " + player.getName() + " does not have access to permission " + permission);
                    else
                        Depends.permissions.playerRemove(world.asString(), player, permission.asString());
                }
                return;
        }
    }
}
