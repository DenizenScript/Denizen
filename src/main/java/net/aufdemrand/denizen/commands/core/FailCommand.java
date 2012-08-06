package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Sets a Script as 'FAILED'. Scripts can be failed multiple times.
 * This can also be checked against with the FAILED requirement.
 * 
 * @author Jeremy Schroeder
 */

public class FailCommand extends AbstractCommand {

	/* FAIL

	/* Arguments: [] - Required, () - Optional 
	 * None.
	 * 
	 * Modifiers: 
	 * ('SCRIPT:[Script Name]') Changes the script from the triggering script to the one specified.
	 * 
	 * Example Usage:
	 * FAIL
	 * FAIL 'SCRIPT:A different script'
	 */

	@Override
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		String theScript = theCommand.getScript();
		
		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {
				
				if (plugin.debugMode) 
					plugin.getLogger().log(Level.INFO, "Processing command " + theCommand.getCommand() + " argument: " + thisArgument);

				/* If the argument is a SCRIPT: modifier */
				if (thisArgument.contains("SCRIPT:")) {
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'specify script'." );
					theScript = thisArgument.split(":", 2)[1];
				}
				
				/* Can't match to anything */
				else if (plugin.debugMode) 
					plugin.getLogger().log(Level.INFO, "...unable to match argument!");
				
			}
		}
		
		int fails = plugin.getAssignments().getInt("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Failed", 0);

		fails++;	
		
		plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Failed", fails);
		plugin.saveSaves();

		return true;
	}

	
	public boolean getScriptFail(Player thePlayer, String theScript, boolean negativeRequirement) {

		boolean outcome = false;

		if (plugin.getSaves().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Failed") != null) { 
			if (plugin.getSaves().getBoolean("Players." + thePlayer.getName() + "." + theScript + "." + "Failed")) outcome = true;
		}

		if (negativeRequirement != outcome) return true;

		return false;

	}
	
}