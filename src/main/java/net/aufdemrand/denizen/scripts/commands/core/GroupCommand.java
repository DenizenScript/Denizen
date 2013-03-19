package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;

import org.bukkit.entity.Player;

public class GroupCommand extends AbstractCommand {

    private enum Action {ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Initialize fields
        Action action = null;
        Player player = scriptEntry.getPlayer();
        String group = null;
        String world = null;

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("ADD, REMOVE", arg)) {
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());
            } else if (aH.matchesValueArg("PLAYER", arg, ArgumentType.String)) {
                player = aH.getPlayerFrom(arg);
            } else if (aH.matchesValueArg("WORLD", arg, ArgumentType.String)) {
                group = aH.getStringFrom(arg);
            } else group = arg;
        }

        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("action", action)
                    .addObject("player", player)
                    .addObject("group", group)
                    .addObject("world", world);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Action action = (Action) scriptEntry.getObject("action");
        Player player = (Player) scriptEntry.getObject("player");
        String group = String.valueOf(scriptEntry.getObject("group"));
        String world = String.valueOf(scriptEntry.getObject("world"));

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Action", action.toString())
                        + aH.debugObj("Action", action.toString())
                        + aH.debugObj("Player", player.getName())
                        + aH.debugObj("Group", group)
                        + aH.debugObj("World", world));

        switch (action) {
        case ADD:
            if(Depends.permissions.playerInGroup(world, player.getName(), group)) {
                dB.echoDebug("Player " + player.getName() + " is already in group " + group); 
            } else Depends.permissions.playerAddGroup(world, player.getName(), group);
            return;
        case REMOVE: 
            if(!Depends.permissions.playerInGroup(world, player.getName(), group)) {
                dB.echoDebug("Player " + player.getName() + " is not in group " + group); 
            } else Depends.permissions.playerRemoveGroup(world, player.getName(), group);
            return;
        }
    }

}