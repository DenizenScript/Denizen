package net.aufdemrand.denizen.commands.core;

import org.bukkit.Location;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.triggers.core.ChatTrigger;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Hints to the Player the available Chat Triggers available. 
 * 
 * @author Jeremy Schroeder
 */

public class HintCommand extends AbstractCommand {

	/* HINT */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * None.
	 *   
	 * Example Usage:
	 * HINT
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		plugin.getTriggerRegistry().getTrigger(ChatTrigger.class).
	
		if (chatTrigger)
		// Execution process.
			// Do whatever you want the command to do, here.
			
			
			/* Command has sucessfully finished */
			return true;
		/* Error processing */
		if (plugin.debugMode)
			throw new CommandException("...no Chat Triggers found!");
			
		return false;
	}

	
}