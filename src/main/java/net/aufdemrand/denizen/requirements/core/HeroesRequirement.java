package net.aufdemrand.denizen.requirements.core;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.requirements.AbstractRequirement;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;

public class HeroesRequirement extends AbstractRequirement {

	/* FLAGGED [NAME]:[VALUE]|[NAME]

	/* Arguments: [] - Required, () - Optional 
	 * [NAME:VALUE]  or  [NAME:++]  or  [NAME:--]
	 * 
	 * Modifiers: 
	 * (DURATION:#) Reverts to the previous head position after # amount of seconds.
	 * 
	 * Example usages:

	 */

	enum ClassType { PRIMARY, SECONDARY, ANY }

	@Override    
	public boolean check(Player thePlayer, DenizenNPC theDenizen, String theScript, String[] arguments, Boolean negativeRequirement)
			throws RequirementMissingException {

		boolean outcome = false;
		ClassType classType = ClassType.ANY;
		String classToCheck = null;

		if (arguments == null)
			throw new RequirementMissingException("USAGE: HEROES_CLASS [CLASS_TYPE] (PRIMARY|SECONDARY)");

		/* Get arguments */
		for (String thisArg : arguments) {

			if (thisArg.contains("PRIMARY")) {
				classType = ClassType.PRIMARY;
				aH.echoDebug("...checking only PRIMARY class.");
			}

			else if (thisArg.contains("SECOND")) {
				classType = ClassType.SECONDARY;
				aH.echoDebug("...checking only SECONDARY class.");
			}

			else {
				classToCheck = thisArg.toUpperCase();
				aH.echoDebug("...checking for '%s' class.", thisArg);
			}
		}

		// Let's check info!

		if (plugin.heroes != null) {
			Hero theHero = plugin.heroes.getCharacterManager().getHero(thePlayer);

			switch (classType) {
			case ANY:
				if (theHero.getSecondClass().getName().toUpperCase().equals(classToCheck)
						|| theHero.getHeroClass().getName().toUpperCase().equals(classToCheck)) {
					outcome = true;
					aH.echoDebug("...class matched!");
				} else {
					aH.echoDebug("...was looking for '" + classToCheck + "', but found '" + theHero.getHeroClass().getName().toUpperCase() + "' and '" + theHero.getSecondClass().getName().toUpperCase() + "'.");
				}
				break;

			case PRIMARY:
				if (theHero.getHeroClass().getName().toUpperCase().equals(classToCheck)) {
					outcome = true;
					aH.echoDebug("...PRIMARY class matched!");
				} else {
					aH.echoDebug("...was looking for '" + classToCheck + "', but found '" + theHero.getHeroClass().getName().toUpperCase() + "'.");
				}
				break;

			case SECONDARY:
				if (theHero.getSecondClass().getName().toUpperCase().equals(classToCheck)) {
					outcome = true;
					aH.echoDebug("...SECONDARY class matched!");
				} else {
					aH.echoDebug("...was looking for '" + classToCheck + "', but found '" + theHero.getSecondClass().getName().toUpperCase() + "'.");
				}
				break;
			}

		} else throw new RequirementMissingException("Could not find Heroes!");

		if (negativeRequirement != outcome) return true;

		return false;
	}
}