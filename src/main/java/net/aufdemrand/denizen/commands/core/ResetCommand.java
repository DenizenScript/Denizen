package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * 
 *  
 * @author Jeremy Schroeder
 */

public class ResetCommand extends AbstractCommand {

	/* RESET ('Name of Script') [FINISHES|FAILS]  or  RESET [FLAG:[NAME]]

	/* Arguments: [] - Required, () - Optional 
	 * [FINISHED|FAILED|FLAG:[NAME]] 
	 * (['Name of Script']) - Required when using FINISHES/FAILS
	 * 
	 * Modifiers: 
	 * None.
	 * 
	 * Example usages:
	 * 
	 * RESET FAILS
	 * RESET 'Example Script 6' FINISHES
	 * RESET 'FLAG:MAGICSHOPITEM'
	 * 
	 */


	private enum ResetType {
		FINISH, FAIL, FLAG
	}

	@Override
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		String theScript = theCommand.getScript();
		ResetType resetType = null;
		String theFlag = null;

		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				if (thisArgument.equals("FINISHES") || thisArgument.equals("FINISHED") || thisArgument.equals("FINISH")) {
					resetType = ResetType.FINISH;
					if (plugin.debugMode) 
						plugin.getLogger().info("Matched " + thisArgument + "...will reset FINISHED.");
				}

				else if (thisArgument.equals("FAILS") || thisArgument.equals("FAIL") || thisArgument.equals("FAILED")) {
					resetType = ResetType.FAIL;
					if (plugin.debugMode) 
						plugin.getLogger().info("Matched " + thisArgument + "...will reset FAILED.");
				}

				else if (thisArgument.toUpperCase().contains("FLAG:")) {
					if (plugin.debugMode) 
						plugin.getLogger().info("Matched " + thisArgument + "...will reset FLAG.");
					theFlag = thisArgument.split(":")[1].toUpperCase();
					resetType = ResetType.FLAG;
				}

				else { 
					theScript = thisArgument;
					if (plugin.debugMode) 
						plugin.getLogger().info("Matched " + thisArgument + "...changed script to be RESET.");
				}
			}
		}


		if (resetType != null) {
			switch (resetType) {

			case FINISH:
				plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Completed", 0);
				break;

			case FAIL:
				plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Failed", 0);
				break;

			case FLAG:
				plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + ".Flags." + theFlag, null);
				break;
			}
			return true;
		}

		throw new CommandException("...not enough arguments! Use RESET ('Name of Script') [FINISHES|FAILS]  or  RESET [FLAG:[NAME]]");
	}


}