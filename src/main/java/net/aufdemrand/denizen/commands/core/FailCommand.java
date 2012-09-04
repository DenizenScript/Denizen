package net.aufdemrand.denizen.commands.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.events.ScriptFailEvent;
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
			for (String thisArg : theCommand.arguments()) {

				// If the argument is a SCRIPT: modifier
				if (aH.matchesScript(thisArg)) {
					theScript = aH.getStringModifier(thisArg);
					aH.echoDebug("...script to fail now '%s'.", thisArg);
				}

				// Can't match to anything
				else aH.echoError("Unable to match '%s'!", thisArg);
			}
		}


		int fails = plugin.getSaves().getInt("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Failed", 0);
		fails++;
		plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Failed", fails);

		ScriptFailEvent event = new ScriptFailEvent(theCommand.getPlayer(), theScript, fails);
		Bukkit.getServer().getPluginManager().callEvent(event);

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