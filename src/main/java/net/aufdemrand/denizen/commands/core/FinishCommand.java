package net.aufdemrand.denizen.commands.core;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Sets a Script as 'FINISHED'. Scripts can be finished multiple times.
 * This can also be checked against with the FINISHED requirement.
 *  
 * @author Jeremy Schroeder
 */

public class FinishCommand extends AbstractCommand {

	/* FINISH ('SCRIPT:[Script Name]')

	/* Arguments: [] - Required, () - Optional 
	 * None.
	 * 
	 * Modifiers: 
	 * ('SCRIPT:[Script Name]') Changes the script from the triggering script to the one specified.
	 * 
	 * Example usages:
	 */

	@Override
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		String theScript = theCommand.getScript();

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArg : theCommand.arguments()) {

				// If the argument is a SCRIPT: modifier
				if (aH.matchesScript(thisArg)) {
					theScript = aH.getStringModifier(thisArg);
					aH.echoDebug("...script to finish now '%s'.", thisArg);
				}

				// Can't match to anything
				else aH.echoError("...unable to match argument!");
			}
		}

		
		/* Write data to saves */
		
		int currentFinishes = plugin.getSaves().getInt("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Completed", 0);
		currentFinishes++;	
		
		plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Completed", currentFinishes);
		plugin.saveSaves();

		return true;
	}

	

	// Requirement
	// TODO: Move to requirements/core

	public boolean getScriptCompletes(Player thePlayer, String theScript, String theAmount, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)FINISHED (#) [Name of Script]
		 */

			if (Character.isDigit(theAmount.charAt(0))) theScript = theScript.split(" ", 2)[1];
			else theAmount = "1";

			if (plugin.getSaves().contains("Players." + thePlayer.getName() + "." + theScript + "." + "Completed")) { 
				if (plugin.getSaves().getInt("Players." + thePlayer.getName() + "." + theScript + "." + "Completed", 0) >= Integer.valueOf(theAmount)) outcome = true;
			}

		if (negativeRequirement != outcome) return true;

		return false;
	}




}