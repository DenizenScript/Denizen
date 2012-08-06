package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Executes a bukkit command
 * 
 * @author Jeremy Schroeder
 */

public class ExecuteCommand extends AbstractCommand {

	/* EXECUTE [ASPLAYER|ASSERVER|ASNPC] '[command with arguments]' */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [ASPLAYER|ASSERVER|ASNPC] defines how the command should be executed.
	 * [command with arguments] is the command to run, without a /. Can also
	 *   use <PLAYER> and <WORLD> variables.
	 *   
	 * Example Usage:
	 * EXECUTE ASSERVER 'gamemode <PLAYER> 2'
	 * EXECUTE ASNPC 'toggledownfall'
	 * 
	 */

	enum ExecuteType { ASSERVER, ASDENIZEN, ASPLAYER }


	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		String commandtoExecute = null;
		ExecuteType executeType = null;

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				if (plugin.debugMode) 
					plugin.getLogger().info("Processing command " + theEntry.getCommand() + " argument: " + thisArgument);

				/* If argument is ASPLAYER */
				if (thisArgument.equalsIgnoreCase("ASPLAYER")) {
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'Execute as Player'.");
					executeType = ExecuteType.ASPLAYER;
				}

				/* If argument is ASNPC */
				else if (thisArgument.equalsIgnoreCase("ASNPC")) {
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'Execute as NPC'.");
					executeType = ExecuteType.ASDENIZEN;
				}

				/* If argument is ASSERVER */
				else if (thisArgument.equalsIgnoreCase("ASSERVER")) {
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'Execute as Console'.");
					executeType = ExecuteType.ASSERVER;
				}

				else {
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument as 'Command to execute'.");
					commandtoExecute = thisArgument
							.replace("<PLAYER>", theEntry.getPlayer().getName())
							.replace("<WORLD>", theEntry.getPlayer().getWorld().getName());
				}
				
			}	
		}

		/* Execute the command, if all required variables are filled. */
		if (commandtoExecute != null && executeType != null) {

			switch (executeType) {

			case ASPLAYER:
				theEntry.getPlayer().performCommand(commandtoExecute);
				break;

			case ASDENIZEN:
				if (theEntry.getDenizen().getCitizensEntity().getBukkitEntity().getType() == EntityType.PLAYER) {
					((Player) theEntry.getDenizen().getEntity()).setOp(true);
					((Player) theEntry.getDenizen().getEntity()).performCommand(commandtoExecute);
					((Player) theEntry.getDenizen().getEntity()).setOp(false);
				} else throw new CommandException("...cannot EXECUTE ASNPC unless NPC is Human!");
				break;

			case ASSERVER:
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandtoExecute);
			}

			return true;
		}

		/* Error processing */
		if (plugin.debugMode) if (theEntry.arguments() == null)
			throw new CommandException("...not enough arguments! Usage: EXECUTE [ASSERVER|ASNPC|ASPLAYER] '[Command to execute]'");

		return false;
	}

}