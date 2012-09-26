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
	 * FLAG 'MAGICSHOPITEM:FEATHER' 'DURATION:60'
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
		Double flagValue = null;
		String flagString = null;
		boolean exactly = false;
		boolean global = false;
		boolean denizen = false;
		boolean checkNumber = false;

		if (arguments == null)
			throw new RequirementMissingException("Must provide a flag to be checked!");

		/* Get arguments */
		for (String thisArgument : arguments) {

			// Integer or String value
			if (thisArgument.split(":").length == 2) {

				aH.echoDebug("...flag to check is '%s'.", thisArgument);

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
				aH.echoDebug("...flag must be EXACT!", thisArgument);
				exactly = true;
			}

			else if (thisArgument.equalsIgnoreCase("GLOBAL")) {
				aH.echoDebug("...checking global flags.");
				global = true;
			}

			else if (thisArgument.equals("CHECKNUMBER")) {
				aH.echoDebug("...checking if flag is a valid NUMBER.");
				checkNumber = true;
			}

			else if (thisArgument.equalsIgnoreCase("PLAYER")) {
				aH.echoDebug("...checking player flags.");
			}

			else if (thisArgument.equalsIgnoreCase("DENIZEN")) {
				aH.echoDebug("...checking denizen flags.");
				denizen = true;
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

					if (checkNumber) {
						if (plugin.getSaves().getString("Global.Flags." + flagName).toUpperCase().matches("\\d+")
								|| plugin.getSaves().getString("Global.Flags." + flagName).toUpperCase().matches("\\d+\\.\\d+")) {
							aH.echoDebug("...found global flag '%s' is a number!", flagName);
							outcome = true;
						} else aH.echoDebug("...global flag '%s' is not a number!", flagName);
					}

					else {
						if (plugin.getSaves().contains("Global.Flags." + flagName)) {

							if (!plugin.getSaves().getString("Global.Flags." + flagName).toUpperCase().equals("FALSE")) {
								outcome = true;
								aH.echoDebug("...global boolean flag '%s'!", flagName);
							} else aH.echoDebug("...global boolean flag '%s' is FALSE!", flagName);
						} else aH.echoDebug("...did not find global boolean flag '%s'!", flagName);
					}
				} 

				else if (denizen) {

					if (checkNumber) {
						if (plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName).toUpperCase().matches("\\d+")
								|| plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName).toUpperCase().matches("\\d+\\.\\d+")) {
							aH.echoDebug("...denizen flag '%s' is a number!", flagName);
							outcome = true;
						} else aH.echoDebug("...denizen flag '%s' is not a number!", flagName);
					}	

					else {
						if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName)) {
							if (!plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName).toUpperCase().equals("FALSE")) {
								outcome = true;
								aH.echoDebug("...found denizen boolean flag '%s'!", flagName);
							} else aH.echoDebug("...denizen boolean flag '%s' is FALSE!", flagName);
						} else aH.echoDebug("...did not find denizen boolean flag '%s'!", flagName);

					}
				}

				else {

					if (checkNumber) {
						if (plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName).toUpperCase().matches("\\d+")
								|| plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName).toUpperCase().matches("\\d+\\.\\d+")) {
							aH.echoDebug("...player flag '%s' is a number!", flagName);
							outcome = true;
						} else aH.echoDebug("...player flag '%s' is not a number!", flagName);
					}	

					else {

						if (plugin.getSaves().contains("Players." + thePlayer.getName() + ".Flags." + flagName)) {
							if (!plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName).toUpperCase().equals("FALSE")) {
								outcome = true;
								aH.echoDebug("...found player boolean flag '%s'!", flagName);
							} else aH.echoDebug("...player boolean flag '%s' is FALSE!", flagName);
						} else aH.echoDebug("...did not find player boolean flag '%s'!", flagName);

					}
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

				else if (denizen) {
					if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName)) {
						if (plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName).toUpperCase()
								.equalsIgnoreCase(flagString)) {
							outcome = true;
							aH.echoDebug("...denizen flag '%s' matched!", flagName);
						} else aH.echoDebug("...denizen flag '%s' did not match!", flagName);

						if (outcome == false) aH.echoDebug("...was looking for '" + flagValue + "', found '" + plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName) + "'.");
					} else aH.echoDebug("...denizen flag '%s' not set!", flagName);
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

						try {
							// Looking for exact number...
							if (exactly) {
								if (Double.valueOf(plugin.getSaves().getString("Global.Flags." + flagName))
										== (flagValue)) {
									outcome = true;
									aH.echoDebug("...global flag '%s' matched!", flagName);
								} else aH.echoDebug("...global flag '%s' did not exactly match!", flagName);
							} else { // Looking for more than or equal...
								if (Double.valueOf(plugin.getSaves().getString("Global.Flags." + flagName))
										>= (flagValue)) {
									outcome = true;
									aH.echoDebug("...global flag '%s' matched!", flagName);
								} else 	aH.echoDebug("...global flag '%s' did not match!", flagName);
							}

						} catch (Exception e) { aH.echoDebug("...flag '%s' did not match!", flagName); }

						if (outcome == false) aH.echoDebug("...was looking for '" + flagValue + "', found '" + plugin.getSaves().getString("Global.Flags." + flagName) + "'.");
					} else aH.echoDebug("...global flag '%s' not set!", flagName);
				} 

				else if (denizen) {

					if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName)) {

						try {
							// Looking for exact number...
							if (exactly) {
								if (Double.valueOf(plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName))
										== (flagValue)) {
									outcome = true;
									aH.echoDebug("...denizen flag '%s' matched!", flagName);
								} else aH.echoDebug("...denizen flag '%s' did not exactly match!", flagName);
							} else { // Looking for more than or equal...
								if (Double.valueOf(plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName))
										>= (flagValue)) {
									outcome = true;
									aH.echoDebug("...denizen flag '%s' matched!", flagName);
								} else aH.echoDebug("...denizen flag '%s' did not match!", flagName);
							}

						} catch (Exception e) { aH.echoDebug("...flag '%s' did not match!", flagName); }

						if (outcome == false) aH.echoDebug("...was looking for '" + flagValue + "', found '" + plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Flags." + flagName) + "'.");
					} else aH.echoDebug("...denizen flag '%s' not set!", flagName);
				}		

				else {
					if (plugin.getSaves().contains("Players." + thePlayer.getName()+ ".Flags." + flagName)) {

						try {
							// Looking for exact number...
							if (exactly) {
								if (Double.valueOf(plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName))
										== (flagValue)) {
									outcome = true;
									aH.echoDebug("...player flag '%s' matched!", flagName);
								} else aH.echoDebug("...player flag '%s' did not exactly match!", flagName);
							} else { // Looking for more than or equal...
								if (Double.valueOf(plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName))
										>= (flagValue)) {
									outcome = true;
									aH.echoDebug("...player flag '%s' matched!", flagName);
								} else aH.echoDebug("...player flag '%s' did not match!", flagName);
							}

						} catch (Exception e) { aH.echoDebug("...flag '%s' did not match!", flagName); }

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