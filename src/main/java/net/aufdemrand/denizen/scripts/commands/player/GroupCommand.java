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

public class GroupCommand extends AbstractCommand {

    private enum Action {ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }

            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", new Element(arg.asType(dWorld.class).getName()));
            }

            else if (!scriptEntry.hasObject("group")) {
                scriptEntry.addObject("group", arg.asElement());
            }

        }

        if (!scriptEntry.hasPlayer() || !scriptEntry.getPlayer().isValid())
            throw new InvalidArgumentsException("Must have player context!");

        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify valid action!");

        if (!scriptEntry.hasObject("group"))
            throw new InvalidArgumentsException("Must specify a group name!");

        scriptEntry.defaultObject("world", Element.NULL);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element action = scriptEntry.getElement("action");
        Element world = scriptEntry.getElement("world");
        Element group = scriptEntry.getElement("group");

        // Report to dB
        dB.report(scriptEntry, getName(), action.debug() + world.debug() + group.debug());

        OfflinePlayer player = scriptEntry.getPlayer().getOfflinePlayer();
        boolean inGroup = Depends.permissions.playerInGroup(world.asString(), player, group.asString());

        switch (Action.valueOf(action.asString().toUpperCase())) {
            case ADD:
                if (inGroup)
                    dB.echoDebug(scriptEntry, "Player " + player.getName() + " is already in group " + group);
                else
                    Depends.permissions.playerAddGroup(world.asString(), player, group.asString());
                return;
            case REMOVE:
                if (!inGroup)
                    dB.echoDebug(scriptEntry, "Player " + player.getName() + " is not in group " + group);
                else
                    Depends.permissions.playerRemoveGroup(world.asString(), player, group.asString());
                return;
        }

    }
}
