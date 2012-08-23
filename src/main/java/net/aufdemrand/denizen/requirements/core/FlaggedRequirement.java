package net.aufdemrand.denizen.requirements.core;

import net.aufdemrand.denizen.requirements.AbstractRequirement;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.entity.LivingEntity;
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
	public boolean check(LivingEntity theEntity, String theScript, String[] arguments, Boolean negativeRequirement)
			throws RequirementMissingException {

		boolean outcome = false;

		if (theEntity instanceof Player) {

			Player thePlayer = (Player) theEntity;

			String flagName = null;
			FlagType flagType = null;
			Integer flagValue = null;
			String flagString = null;
			boolean exactly = false;
			boolean global = false;

			/* Get arguments */
			if (arguments != null) {
				for (String thisArgument : arguments) {

					// Integer or String value
					if (thisArgument.split(":").length == 2) {
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
						exactly = true;
					}

					else if (thisArgument.equalsIgnoreCase("GLOBAL")) {
						global = true;
					}

					// Boolean value
					else {
						flagType = FlagType.BOOLEAN;
						flagName = thisArgument.toUpperCase();
					}
				}
			}

			// Let's check info!

			if (flagType != null) {

				switch (flagType) {

				case BOOLEAN:

					if (global) {
						if (plugin.getSaves().contains("Global.Flags." + flagName)) 
							if (!plugin.getSaves().getString("Global.Flags." + flagName).toUpperCase()
									.equals("FALSE")) outcome = true;
					} else {
						if (plugin.getSaves().contains("Players." + thePlayer.getName() + ".Flags." + flagName)) 
							if (!plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName).toUpperCase()
									.equals("FALSE")) outcome = true;
					}

					break;

				case STRING:
					if (global) {
						if (plugin.getSaves().contains("Global.Flags." + flagName)) 
							if (plugin.getSaves().getString("Global.Flags." + flagName).toUpperCase()
									.equalsIgnoreCase(flagString)) outcome = true;
					} else {
						if (plugin.getSaves().contains("Players." + thePlayer.getName()+ ".Flags." + flagName)) 
							if (plugin.getSaves().getString("Players." + thePlayer.getName()+ ".Flags." + flagName).toUpperCase()
									.equalsIgnoreCase(flagString)) outcome = true;
					}
					break;

				case INTEGER:
					if (global) {
						if (plugin.getSaves().contains("Global.Flags." + flagName)) {
							// Looking for exact number...
							if (exactly) {
								if (plugin.getSaves().getInt("Global.Flags." + flagName)
										== (flagValue)) outcome = true;
							} else { // Looking for more than or equal...
								if (plugin.getSaves().getInt("Global.Flags." + flagName)
										>= (flagValue)) outcome = true;
							}
						}
					} else {
						if (plugin.getSaves().contains("Players." + thePlayer.getName()+ ".Flags." + flagName)) {
							// Looking for exact number...
							if (exactly) {
								if (plugin.getSaves().getInt("Players." + thePlayer.getName()+ ".Flags." + flagName)
										== (flagValue)) outcome = true;
							} else { // Looking for more than or equal...
								if (plugin.getSaves().getInt("Players." + thePlayer.getName()+ ".Flags." + flagName)
										>= (flagValue)) outcome = true;
							}
						}
					}
					break;

				}

			}

		}

		if (negativeRequirement != outcome) return true;

		return false;
	}


}