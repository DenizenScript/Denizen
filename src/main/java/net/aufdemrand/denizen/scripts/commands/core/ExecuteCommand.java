package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

public class ExecuteCommand extends AbstractCommand {

	enum ExecuteType { AS_SERVER, AS_NPC, AS_PLAYER, AS_OP }

	String command = null;
	ExecuteType executeType = null;
	LivingEntity target = null;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		
		// Parse arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesArg("ASPLAYER, AS_PLAYER", arg)) {
				executeType = ExecuteType.AS_PLAYER;
				target = scriptEntry.getPlayer();
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);

            }   else if (aH.matchesArg("ASOPPLAYER, ASOP, AS_OP, AS_OP_PLAYER", arg)) {
				executeType = ExecuteType.AS_OP;
				target = scriptEntry.getPlayer();
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);

            }   else if (aH.matchesArg("ASNPC, AS_NPC", arg)) {
				executeType = ExecuteType.AS_NPC;
				target = scriptEntry.getNPC().getEntity();
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);

            }   else if (aH.matchesArg("ASSERVER, AS_SERVER", arg)) {
				executeType = ExecuteType.AS_SERVER;
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);

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
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

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
				throw new CommandExecutionException("Cannot EXECUTE AS_NPC unless the NPC is Player-Type.");
			((Player) target).setOp(true);
			((Player) target).performCommand(command);
			((Player) target).setOp(false);
			return;
			
		case AS_SERVER:
			denizen.getServer().dispatchCommand(denizen.getServer().getConsoleSender(), command);
        }
	}
	
}