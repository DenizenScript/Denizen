package net.aufdemrand.denizen.commands.core;

import org.bukkit.Location;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Your command! 
 * This class is a template for a Command in Denizen.
 * 
 * @author You!
 */

public class SampleCommand extends AbstractCommand {

	/* COMMAND_NAME [TYPICAL] (ARGUMENTS) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [TYPICAL] argument with a description if necessary.
	 * (ARGUMENTS) should be clear and concise.
	 *   
	 * Modifiers:
	 * (MODIFIER:VALUE) These are typically advanced usage arguments.
	 * (DURATION:#) They should always be optional. Use standard modifiers
	 *   already established if at all possible.
	 *   
	 * Example Usage:
	 * COMMAND_NAME VALUE
	 * COMMAND_NAME DIFFERENTVALUE OPTIONALVALUE
	 * COMMAND_NAME ANOTHERVALUE 'MODIFIER:Show one-line examples.'
	 * 
	 */

	@SuppressWarnings("unused") // This should be removed from your code.
	@Override
	// This is the method that is called when your command is ready to be executed.
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

	    	// Typically initialized as null and filled as needed. Remember: theEntry
		    // contains some information passed through the execution process.
			Boolean requiredVariable = null;
			Location sampleBookmark = null;
			
		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArg : theEntry.arguments()) {
				
				// Do this routine for each argument supplied.
				// Includes are some typical arguments. Modify/add code to handle your command needs.
				
				// If argument is a number.
				if (aRegex.matchesInteger(thisArg)) {
					// Insert code here.
					
					echoDebug("...number argument set to '%s'.", thisArg);
				}
					
				else if (aRegex.matchesScript(thisArg)) {
					// Insert code here.
					
					echoDebug("...affected script now set to '%s'.", thisArg);
				}
				
				else if (aRegex.matchesDuration(thisArg)) {
					// Insert code here.
					
					echoDebug("...duration set to '%s'.", thisArg);
				}
				
				// If can't match to anything...
				// This isn't always possible, depending on the arguments your 
				// command uses, but nice if you can. Keep the users informed!
				else {
					echoDebug("...'%s' could not be matched!", thisArg);
				}
				
			}	
		}

		/* Execute the command, if all required variables are filled. */
		if (requiredVariable != null) {
			
			
			// Execution process.
			// Do whatever you want the command to do, here.
			
			
			/* Command has sucessfully finished */
			return true;
		}
			
		// else...
		
		/* Error processing */
			
			// Processing has gotten to here, there's probably not been enough arguments. 
			// Let's alert the console.
		if (plugin.debugMode) if (theEntry.arguments() == null)
			throw new CommandException("...not enough arguments! Usage: SAMPLECOMMAND [TYPICAL] (ARGUMENTS)");
			
		return false;
	}

	
}