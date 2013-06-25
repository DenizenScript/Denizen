package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;

import org.bukkit.entity.Player;

public class GroupCommand extends AbstractCommand {

    private enum Action {ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Initialize fields
        Action action = null;
        String group = null;
        String world = null;

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("ADD, REMOVE", arg)) {
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());
            } else if (aH.matchesValueArg("WORLD", arg, ArgumentType.String)) {
                world = aH.getStringFrom(arg);
            } else group = arg;
        }

        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("action", action)
                    .addObject("group", group)
                    .addObject("world", world);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Action action = (Action) scriptEntry.getObject("action");
        String group = String.valueOf(scriptEntry.getObject("group"));
        String world = String.valueOf(scriptEntry.getObject("world"));
        Player player = scriptEntry.getPlayer().getPlayerEntity();

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Action", action.toString())
                        + aH.debugObj("Player", player.getName())
                        + aH.debugObj("Group", group)
                        + aH.debugObj("World", world));

        switch (action) {
        case ADD:
            if(Depends.permissions.playerInGroup(world, player.getName(), group)) {
                dB.echoDebug("Player " + player.getName() + " is already in group " + group); 
            }
            else {
            	Depends.permissions.playerAddGroup(world, player.getName(), group);
            	dB.echoDebug("Added " + player.getName() + " to group " + group);
            }
            return;
        case REMOVE: 
            if(!Depends.permissions.playerInGroup(world, player.getName(), group)) {
                dB.echoDebug("Player " + player.getName() + " is not in group " + group); 
            }
            else {
            	Depends.permissions.playerRemoveGroup(world, player.getName(), group);
            	dB.echoDebug("Removed " + player.getName() + " from group " + group);
            }
            return;
        }
    }

}