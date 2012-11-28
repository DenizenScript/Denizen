package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEngine;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.entity.Player;

public class FlaggedRequirement extends AbstractRequirement {


	/* FLAGGED [NAME]:[VALUE]|[NAME]

	/* Arguments: [] - Required, () - Optional 
	 * [NAME:VALUE]  or  [NAME:++]  or  [NAME:--]
	 * 
	 * Modifiers: 
	 * (DURATION:#) Reverts to the previous head position after # amount of seconds.
	 * 
	 * Example usages:
	 * FLAG 'MAGICSHOPITEM:FEATHER' 'DURATION:60'
	 * FLAG 'HOSTILECOUNT:++'
	 * FLAG 'ALIGNMENT:--'
	 * FLAG 'CUSTOMFLAG:SET'
	 */

	enum FlagType { STRING, INTEGER, BOOLEAN }

	@Override
	public boolean check(Player thePlayer, DenizenNPC theDenizen, String theScript, String[] arguments)
			throws RequirementCheckException {

	    boolean outcome = false;

		String flagName = null;
		FlagType flagType = null;
		Double flagValue = null;
		String flagString = null;
		boolean exactly = false;
		boolean global = false;
		boolean denizen = false;
		boolean checkNumber = false;

		if (arguments == null)
			throw new RequirementCheckException("Must provide a flag to be checked!");

		/* Get arguments */
		for (String thisArgument : arguments) {

			// Integer or String value
			if (thisArgument.split(":").length == 2) {

				dB.echoDebug("...flag to check is '%s'.", thisArgument);

				if (thisArgument.split(":")[1].matches("\\d+\\.\\d+")
						|| thisArgument.split(":")[1].matches("\\d+")) {
					flagType = FlagType.INTEGER;
					flagName = thisArgument.split(":")[0].toUpperCase();
					flagValue = Double.valueOf(thisArgument.split(":")[1]);
				} else {
					flagType = FlagType.STRING;
					flagName = thisArgument.split(":")[0].toUpperCase();
					flagString = thisArgument.split(":")[1];
				}
			}

			else if (thisArgument.equalsIgnoreCase("EXACTLY")) {
				dB.echoDebug("...flag must be EXACT!", thisArgument);
				exactly = true;
			}

			else if (thisArgument.equalsIgnoreCase("GLOBAL")) {
				dB.echoDebug("...checking global flags.");
				global = true;
			}

			else if (thisArgument.equals("CHECKNUMBER") || thisArgument.equals("IS_NUMBER") ) {
				dB.echoDebug("...checking if flag is a valid NUMBER.");
				flagType = FlagType.BOOLEAN;
				checkNumber = true;
			}

			else if (thisArgument.equalsIgnoreCase("PLAYER")) {
				dB.echoDebug("...checking player flags.");
			}

			else if (thisArgument.equalsIgnoreCase("DENIZEN")) {
				dB.echoDebug("...checking denizen flags.");
				denizen = true;
			}

			// Boolean value
			else {
				dB.echoDebug("...flag to check is '%s'.", thisArgument);
				flagType = FlagType.BOOLEAN;
				flagName = thisArgument.toUpperCase();
			}
		}

		// Let's check info!

    	return outcome;
	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
    }
}