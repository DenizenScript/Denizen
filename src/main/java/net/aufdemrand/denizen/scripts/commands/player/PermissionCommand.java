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


public class PermissionCommand extends AbstractCommand {

    private enum Action {ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Initialize fields
        Action action = null;
        Player player = scriptEntry.getPlayer().getPlayerEntity();
        String permission = null;
        String group = null;
        String world = null;

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("ADD, REMOVE", arg)) {
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());
            } else if (aH.matchesValueArg("GROUP", arg, ArgumentType.String)) {
                group = aH.getStringFrom(arg);
            } else if (aH.matchesValueArg("WORLD", arg, ArgumentType.String)) {
                world = aH.getStringFrom(arg);
            } else permission = arg;

        }

        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("action", action)
                    .addObject("player", player)
                    .addObject("permission", permission)
                    .addObject("group", group)
                    .addObject("world", world);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Action action = (Action) scriptEntry.getObject("action");
        Player player = (Player) scriptEntry.getObject("player");
        String permission = String.valueOf(scriptEntry.getObject("permission"));
        String group = String.valueOf(scriptEntry.getObject("group"));
        String world = String.valueOf(scriptEntry.getObject("world"));
        
        if(group.equals("null"))
            group = null;
        if(world.equals("null"))
            world = null;

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Action", action.toString())
                        + aH.debugObj("Player", player.getName())
                        + aH.debugObj("Permission", permission)
                        + (group != null ? aH.debugObj("Group", group) : "")
                        + (world != null ? aH.debugObj("World", world) : ""));
		

        switch (action) {
        case ADD:
            if(group != null) {
                if(Depends.permissions.groupHas(world, group, permission)) {
                    dB.echoDebug("Group " + group + " already has permission " + permission);
                }
                else {
                	Depends.permissions.groupAdd(world, group, permission);
                	dB.echoDebug("Added permission " + permission + " for group " + group);
                }
            } else {
                if(Depends.permissions.has(player, permission)) {
                    dB.echoDebug("Player " + player.getName() + " already has permission " + permission);
                }
                else {
                	Depends.permissions.playerAdd(player, permission);
                	dB.echoDebug("Added permission " + permission + " for player " + player.getName());
                }
            }
            return;
        case REMOVE: 
            if(group != null) {
                if(!Depends.permissions.groupHas(world, group, permission)) {
                    dB.echoDebug("Group " + group + " does not have access to permission " + permission);
                }
                else {
                	Depends.permissions.groupRemove(world, group, permission);
                	dB.echoDebug("Removed permission " + permission + " for group " + group);
                }
            } else {
                if(!Depends.permissions.has(player, permission)) {
                    dB.echoDebug("Player " + player.getName() + " does not have access to permission " + permission);
                }
                else {
                	Depends.permissions.playerRemove(world, player.getName(), permission);
                	dB.echoDebug("Removed permission " + permission + " for player " + player.getName());
                }
            }
            return;
        }
    }

}
