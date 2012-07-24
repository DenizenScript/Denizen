package net.aufdemrand.denizen.commands.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.DenizenCommand;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;
import net.citizensnpcs.command.exception.CommandException;

/**
 * 
 * 
 * 
 * @author Jeremy Schroeder
 *
 */

public class FinishCommand extends DenizenCommand {

	/* 

	/* Arguments: [] - Required, () - Optional 
	 *  
	 * 
	 * Modifiers: 
	 * 
	 */

	@Override
	public boolean execute(ScriptCommand theCommand) throws CommandException {

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				/* If number argument... */
				if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					

				/* If modifier... */
				else if (thisArgument.contains("SCRIPT:")) 


			}
		}


		
		
		throw new CommandException("Unknown error, check syntax!");
	}

	
	/* 
	 * GetScriptComplete/GetScriptFail
	 *
	 * Requires the Player and the Script.
	 * Reads the config.yml to find if the player has completed or failed the specified script.
	 *
	 */

	public boolean getScriptCompletes(Player thePlayer, String theScript, String theAmount, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)FINISHED (#) [Name of Script]
		 */

		try {

			if (Character.isDigit(theAmount.charAt(0))) theScript = theScript.split(" ", 2)[1];
			else theAmount = "1";

			if (plugin.getSaves().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Completed") != null) { 
				if (plugin.getSaves().getInt("Players." + thePlayer.getName() + "." + theScript + "." + "Completed", 0) >= Integer.valueOf(theAmount)) outcome = true;
			}

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured with the FINISHED requirement.");
			Bukkit.getLogger().info("Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}




}