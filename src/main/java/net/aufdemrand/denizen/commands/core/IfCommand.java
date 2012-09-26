package net.aufdemrand.denizen.commands.core;

import java.util.List;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.requirements.core.FlaggedRequirement;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Runs a task script if a flag is met.
 * 
 * @author Jeremy Schroeder
 */

public class IfCommand extends AbstractCommand {

	/* IF (EXACTLY) (-)[FLAG] (APPEND) [SCRIPT:Task Script to Run] (QUEUETYPE:TRIGGER|TASK) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * (-) Inverses logic.
	 * [FLAG] The flag that must return true.
	 *   
	 * Example Usage:
	 * IF adventureFlag 'SCRIPT:A Task Script'
	 * IF -adventureFlag 'SCRIPT:Other Task Script'
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 
		boolean invertedLogic = false;
		String theScript = null;
		boolean flagFound = false;
		String theFlag = null;
		String theValue = null;
		boolean global = false;
		boolean exactly = false;
		QueueType queueType = theEntry.sendingQueue();
		boolean inject = true;
		boolean denizen = false;
		boolean checkNumber = false;
		String elseScript = null;
		boolean elseFound = false;

		if (theEntry.arguments() == null) {
			aH.echoError("No arguments! Usage: IF (EXACTLY) [FLAG:FLAG_NAME] (APPEND) [SCRIPT:Task Script to Run] (QUEUETYPE:TRIGGER|TASK)");
			return false;
		}

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			// Fill replaceables
			if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);

			// FLAG IF
			if (thisArg.toUpperCase().contains("FLAG:") && !flagFound) {
				if (thisArg.startsWith("-")) {
					invertedLogic = true;
					aH.echoDebug("...logic inverted, checking inverse.");
					thisArg.substring(1, thisArg.length() - 1);
				}

				/* If argument is a flag with value */
				if (thisArg.split(":").length == 3) {
					theFlag = thisArg.split(":")[1].toUpperCase();
					theValue = thisArg.split(":")[2].toUpperCase();
					aH.echoDebug("...using '%s'.", thisArg.toUpperCase());
					flagFound = true;
				}
				/* Otherwise, argument is a Boolean */
				else {
					theFlag = aH.getStringModifier(thisArg.toUpperCase());
					flagFound = true;
					aH.echoDebug("...using Boolean '%s'.", thisArg.toUpperCase());
				}
			}

			else if (thisArg.toUpperCase().contains("IS_NUMBER")) {
				checkNumber = true;
				aH.echoDebug("...will check if FLAG is a number.", thisArg.toUpperCase());
			}

			else if (thisArg.toUpperCase().equals("ELSE")) {
				elseFound = true;
			}

			else if (thisArg.toUpperCase().contains("GLOBAL")) {
				global = true;
				denizen = false;
				aH.echoDebug("...flag check will be GLOBAL.");
			}

			else if (thisArg.toUpperCase().contains("DENIZEN")) {
				denizen = true;
				global = false;
				aH.echoDebug("...flag check will be for DENIZEN.");
			}

			else if (thisArg.toUpperCase().contains("PLAYER")) {
				denizen = false;
				global = false;
				aH.echoDebug("...flag check will be for PLAYER.");
			}

			else if (aH.matchesQueueType(thisArg)) {
				queueType = aH.getQueueModifier(thisArg);
				aH.echoDebug("...affect will be on '%s'.", thisArg.toString());
			}

			else if (thisArg.equalsIgnoreCase("EXACTLY")) {
				aH.echoDebug("...flag must be EXACT!");
				exactly = true;
			}



			// LITERAL IF (STRING/
			else if (thisArg.toUpperCase().contains("LITERAL:")) {

			}

			else if (thisArg.equalsIgnoreCase("APPEND")) {
				aH.echoDebug("...will APPEND script!");
				inject = false;
			}

			else if (aH.matchesScript(thisArg)) {

				if (!elseFound) {
					theScript = aH.getStringModifier(thisArg);
					aH.echoDebug("...script to run if true is '%s'.", thisArg);
				} else {
					elseScript = aH.getStringModifier(thisArg);
					aH.echoDebug("...script to run if false is '%s'.", thisArg);
				}
			}

			// Can't match to anything
			else aH.echoError("...unable to match argument '%s'!", thisArg);
		}	


		if (theFlag == null || theScript == null) {
			aH.echoError("Too few arguments! Usage: IF [FLAG] [SCRIPT:Task Script to Run]");
			return false;
		}

		if (theValue == null) theValue = "TRUE";

		String[] arguments = null;

		if (checkNumber && denizen) {
			String[] argument = {theFlag + ":" + theValue, "CHECKNUMBER", "DENIZEN"};
			arguments = argument;
		}
		else if (checkNumber && global) {
			String[] argument = {theFlag + ":" + theValue, "CHECKNUMBER", "GLOBAL"};
			arguments = argument;
		}
		else if (checkNumber) {
			String[] argument = {theFlag + ":" + theValue, "CHECKNUMBER"};
			arguments = argument;
		}
		else if (exactly && denizen) {
			String[] argument = {theFlag + ":" + theValue, "EXACTLY", "DENIZEN"};
			arguments = argument;
		}
		else if (exactly && global) {
			String[] argument = {theFlag + ":" + theValue, "EXACTLY", "GLOBAL"};
			arguments = argument;
		}
		else if (exactly) {
			String[] argument = {theFlag + ":" + theValue, "EXACTLY"};
			arguments = argument;
		}
		else if (denizen) {
			String[] argument = {theFlag + ":" + theValue, "DENIZEN"};
			arguments = argument;
		}
		else if (global) {
			String[] argument = {theFlag + ":" + theValue, "GLOBAL"};
			arguments = argument;
		}
		else {
			String[] argument = {theFlag + ":" + theValue};
			arguments = argument;
		}

		/* Execute the command, if all required variables are filled. */
		if (plugin.getRequirementRegistry().getRequirement(FlaggedRequirement.class)
				.check(theEntry.getPlayer(), theEntry.getDenizen(), theScript, arguments, invertedLogic)) {

			aH.echoDebug("...logic met, adding script to be executed.");
			ScriptHelper sE = plugin.getScriptEngine().helper;
			List<String> theScriptEntries = sE.getScript(theScript + ".Script");
			if (theScriptEntries.isEmpty()) return false;

			if (!inject)
				sE.queueScriptEntries(theEntry.getPlayer(), sE.buildScriptEntries(theEntry.getPlayer(), theEntry.getDenizen(), theScriptEntries, theScript, 1), queueType);	
			else
				plugin.getScriptEngine().injectToQueue(theEntry.getPlayer(), sE.buildScriptEntries(theEntry.getPlayer(), theEntry.getDenizen(), theScriptEntries, theScript, 1), queueType, 0);
		} 
		
		// Run elseScript
		else if (elseScript != null) {
			aH.echoDebug("...logic met, adding script to be executed.");
			ScriptHelper sE = plugin.getScriptEngine().helper;
			List<String> theScriptEntries = sE.getScript(elseScript + ".Script");
			if (theScriptEntries.isEmpty()) return false;

			if (!inject)
				sE.queueScriptEntries(theEntry.getPlayer(), sE.buildScriptEntries(theEntry.getPlayer(), theEntry.getDenizen(), theScriptEntries, theScript, 1), queueType);	
			else
				plugin.getScriptEngine().injectToQueue(theEntry.getPlayer(), sE.buildScriptEntries(theEntry.getPlayer(), theEntry.getDenizen(), theScriptEntries, theScript, 1), queueType, 0);
			
		}

		return true;
	}




}