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

		// Typically initialized as null and filled as needed. Remember: theEntry
		// contains some information passed through the execution process.
		String commandtoExecute = null;
		ExecuteType executeType = null;

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				// Do this routine for each argument supplied.

				if (plugin.debugMode) plugin.getLogger().info("Processing command " + theEntry.getCommand() + " argument: " + thisArgument);

				if (thisArgument.equalsIgnoreCase("ASPLAYER")) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...executing command as Player.");
					executeType = ExecuteType.ASPLAYER;
				}

				else if (thisArgument.equalsIgnoreCase("ASNPC")) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...executing command as NPC.");
					executeType = ExecuteType.ASDENIZEN;
				}

				else if (thisArgument.equalsIgnoreCase("ASSERVER")) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...executing command as Console.");
					executeType = ExecuteType.ASSERVER;
				}

				else {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...set command to execute!");
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

		// else...

		/* Error processing */

		// Processing has gotten to here, there's probably not been enough arguments. 
		// Let's alert the console.
		if (plugin.debugMode) if (theEntry.arguments() == null)
			throw new CommandException("...not enough arguments! Usage: EXECUTE [ASSERVER|ASNPC|ASPLAYER] '[Command to execute]'");

		return false;
	}

}