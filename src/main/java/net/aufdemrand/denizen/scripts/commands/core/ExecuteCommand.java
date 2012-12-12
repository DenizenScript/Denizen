package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;

/**
 * Executes a bukkit command.
 * 
 * @author Jeremy Schroeder
 * Version 1.0 Last Updated 11/29 9:57
 */

public class ExecuteCommand extends AbstractCommand {

    @Override
    public void onEnable() {
        // Nothing to do here.
    }

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * ['AS_PLAYER'|'AS_SERVER'|'AS_NPC'|'AS_OP'] defines how the command should be executed.
	 * ['Bukkit Command'] is the command to run, with arguments, without a /.
	 * 
	 * Example Usage:
	 * EXECUTE AS_SERVER 'gamemode <PLAYER.NAME> 2'
	 * EXECUTE AS_NPC 'toggledownfall'
	 */

	enum ExecuteType { AS_SERVER, AS_NPC, AS_PLAYER, AS_OP }

	String command = null;
	ExecuteType executeType = null;
	LivingEntity target = null;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		
		// Parse arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesArg("ASPLAYER", arg)|| aH.matchesArg("AS_PLAYER", arg)) {
				executeType = ExecuteType.AS_PLAYER;
				target = scriptEntry.getPlayer();
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);
				continue;

			}   else if (aH.matchesArg("ASOPPLAYER", arg) || aH.matchesArg("AS_OP_PLAYER", arg)) {
				executeType = ExecuteType.AS_OP;
				target = scriptEntry.getPlayer();
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);
				continue;

			}   else if (aH.matchesArg("ASNPC", arg) || aH.matchesArg("AS_NPC", arg)) {
				executeType = ExecuteType.AS_NPC;
				target = scriptEntry.getNPC().getEntity();
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);
				continue;

			}   else if (aH.matchesArg("ASSERVER", arg)	|| aH.matchesArg("AS_SERVER", arg)) {
				executeType = ExecuteType.AS_SERVER;
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);
				continue;

			}	else {
				command = arg;
				dB.echoDebug(Messages.DEBUG_SET_COMMAND, arg);
			}
		}	

		if (executeType == null)
			throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "EXECUTE_TYPE");

		if (command == null)
			throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "COMMAND_TEXT");

	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {

		switch (executeType) {

		case AS_PLAYER:
			((Player) target).performCommand(command);
			return;
			
		case AS_OP:
			boolean isOp = false;
			if (((Player) target).isOp()) isOp = true;
			if (!isOp) ((Player) target).setOp(true);
			((Player) target).performCommand(command);
			if (!isOp) ((Player) target).setOp(false);
			return;
			
		case AS_NPC:
			if (target.getType() != EntityType.PLAYER) 
				throw new CommandExecutionException("Cannot EXECUTE AS_NPC unless the NPC is Human (Player) type.");
			((Player) target).setOp(true);
			((Player) target).performCommand(command);
			((Player) target).setOp(false);
			return;
			
		case AS_SERVER:
			denizen.getServer().dispatchCommand(denizen.getServer().getConsoleSender(), command);
			return;
		}		
	}
	
}