package net.aufdemrand.denizen.requirements.core;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.requirements.AbstractRequirement;
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
	 * FLAGG 'MAGICSHOPITEM:FEATHER' 'DURATION:60'
	 * FLAG 'HOSTILECOUNT:++'
	 * FLAG 'ALIGNMENT:--'
	 * FLAG 'CUSTOMFLAG:SET'
	 */

	enum FlagType { STRING, INTEGER, BOOLEAN }

	@Override
	public boolean check(Player thePlayer, DenizenNPC theDenizen, String theScript, String[] arguments, Boolean negativeRequirement)
			throws RequirementMissingException {

		boolean outcome = false;


		String flagName = null;
		FlagType flagType = null;
		Integer flagValue = null;
		String flagString = null;
		boolean exactly = false;
		boolean global = false;

		if (arguments == null)
			throw new RequirementMissingException("Must provide a flag to be checked!");

		/* Get arguments */
		for (String thisArgument : arguments) {

			// Integer or String value
			if (thisArgument.split(":").length == 2) {

				aH.echoDebug("...flag to check is '%s'.", thisArgument);

				if (thisArgument.split(":")[1].matches("\\d+")) {
					flagType = FlagType.INTEGER;
					flagName = thisArgument.split(":")[0].toUpperCase();
					flagValue = Integer.valueOf(thisArgument.split(":")[1]);
				} else {
					flagType = FlagType.STRING;
					flagName = thisArgument.split(":")[0].toUpperCase();
					flagString = thisArgument.split(":")[1];
				}
			}

			else if (thisArgument.equalsIgnoreCase("EXACTLY")) {
				aH.echoDebug("...flag must be EXACT!", thisArgument);
				exactly = true;
			}

			else if (thisArgument.equalsIgnoreCase("GLOBAL")) {
				aH.echoDebug("...checking global flags.");
				global = true;
			}

			// Boolean value
			else {
				aH.echoDebug("...flag to check is '%s'.", thisArgument);
				flagType = FlagType.BOOLEAN;
				flagName = thisArgument.toUpperCase();
			}
		}

		// Let's check info!

		if (flagType != null) {
			switch (flagType) {
			case BOOLEAN:
				if (global) {
					if (plugin.getSaves().contains("Global.Flags." + flagName)) {
						if (!plugin.getSaves().getString("Global.Flags." + flagName).toUpperCase().equals("FALSE")) {
							outcome = true;
							aH.echoDebug("...found global boolean flag '%s'!", flagName);
						} else aH.echoDebug("...global boolean flag '%s' is FALSE!", flagName);
					} else aH.echoDebug("...did not find global boolean flag '%s'!", flagName);
				} 
				else {
					if (plugin.getSaves().contains("Players." + thePlayer.getName() + ".Flags." + flagName)) {
						if (!plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName).toUpperCase().equals("FALSE")) {
							outcome = true;
							aH.echoDebug("...found player boolean flag '%s'!", flagName);
						} else aH.echoDebug("...player boolean flag '%s' is FALSE!", flagName);
					} else aH.echoDebug("...did not find player boolean flag '%s'!", flagName);
				}
				break;

			case STRING:
				if (global) {
					if (plugin.getSaves().contains("Global.Flags." + flagName)) {
						if (plugin.getSaves().getString("Global.Flags." + flagName).toUpperCase()
								.equalsIgnoreCase(flagString)) {
							outcome = true;
							aH.echoDebug("...global flag '%s' matched!", flagName);
						} else aH.echoDebug("...global flag '%s' did not match!", flagName);
						if (outcome == false) aH.echoDebug("...was looking for '" + flagValue + "', found '" + plugin.getSaves().getString("Global.Flags." + flagName) + "'.");

					} else aH.echoDebug("...global flag '%s' not set!", flagName);
				} 
				else {
					if (plugin.getSaves().contains("Players." + thePlayer.getName() + ".Flags." + flagName)) {
						if (plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName).toUpperCase()
								.equalsIgnoreCase(flagString)) {
							outcome = true;
							aH.echoDebug("...player flag '%s' matched!", flagName);
						} else aH.echoDebug("...player flag '%s' did not match!", flagName);
						
						if (outcome == false) aH.echoDebug("...was looking for '" + flagValue + "', found '" + plugin.getSaves().getString("Players." + thePlayer.getName() + ".Flags." + flagName) + "'.");
					} else aH.echoDebug("...player flag '%s' not set!", flagName);
				}
				break;

			case INTEGER:
				if (global) {
					if (plugin.getSaves().contains("Global.Flags." + flagName)) {
						// Looking for exact number...
						if (exactly) {
							if (Integer.valueOf(plugin.getSaves().getString("Global.Flags." + flagName))
									== (flagValue)) {
								outcome = true;
								aH.echoDebug("...global flag '%s' matched!", flagName);
							} else aH.echoDebug("...global flag '%s' did not exactly match!", flagName);
						} else { // Looking for more than or equal...
							if (Integer.valueOf(plugin.getSaves().getString("Global.Flags." + flagName))
									>= (flagValue)) {
								outcome = true;
								aH.echoDebug("...global flag '%s' matched!", flagName);
							} else 	aH.echoDebug("...global flag '%s' did not match!", flagName);
						}
						
						if (outcome == false) aH.echoDebug("...was looking for '" + flagValue + "', found '" + plugin.getSaves().getString("Global.Flags." + flagName) + "'.");
					} else aH.echoDebug("...global flag '%s' not set!", flagName);
				} 

				else {
					if (plugin.getSaves().contains("Players." + thePlayer.getName()+ ".Flags." + flagName)) {
						// Looking for exact number...
						if (exactly) {
							if (Integer.valueOf(plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName))
									== (flagValue)) {
								outcome = true;
								aH.echoDebug("...player flag '%s' matched!", flagName);
							} else aH.echoDebug("...player flag '%s' did not exactly match!", flagName);
						} else { // Looking for more than or equal...
							if (Integer.valueOf(plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName))
									>= (flagValue)) {
								outcome = true;
								aH.echoDebug("...player flag '%s' matched!", flagName);
							} else aH.echoDebug("...player flag '%s' did not match!", flagName);
						}
						
						if (outcome == false) aH.echoDebug("...was looking for '" + flagValue + "', found '" + plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName) + "'.");
					} else aH.echoDebug("...player flag '%s' not set!", flagName);
				}		

				break;
			}
		}

		if (negativeRequirement != outcome) return true;
		return false;
	}
}