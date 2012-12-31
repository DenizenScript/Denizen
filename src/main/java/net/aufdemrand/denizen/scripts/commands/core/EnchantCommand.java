package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;

/**
 * Applies an enchantment on an ItemStack
 * 
 * @author Jeremy Schroeder
 */

public class EnchantCommand extends AbstractCommand {

	@Override
	public void onEnable() {

	}

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * 
	 * 
	 * 
	 * Example Usage:
	 * 
	 */
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		
		for (String arg : scriptEntry.getArguments()) {

		
		}

	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

	}
	
	
}