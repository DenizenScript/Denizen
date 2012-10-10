package net.aufdemrand.denizen.requirements.core;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.requirements.AbstractRequirement;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.entity.Player;

public class ScriptRequirement extends AbstractRequirement {


	/* SCRIPT [FINISHED|FAILED|STEP] (STEP:#) (QTY:#) (EXACTLY) [SCRIPT:Script name]

	/* Arguments: [] - Required, () - Optional 
	 * [BOOKMARK:block] block bookmark to check if liquid.
	 * 
	 * Example usages:
	 * ISLIQUID 'BOOKMARK:block'
	 */

	enum ScriptCheck { FINISHED, FAILED, STEP }

	@Override
	public boolean check(Player thePlayer, DenizenNPC theDenizen, String theScript, String[] arguments, Boolean negativeRequirement)
			throws RequirementMissingException {

		boolean outcome = false;

		ScriptCheck scriptCheck = null;
		Integer step = null;
		Integer quantity = null;
		boolean exactly = false;
		String checkScript = null;

		if (arguments == null)
			throw new RequirementMissingException("Not enough arguments! Usage... SCRIPT [FINISHED|FAILED|STEP] (STEP:#) (QTY:#) (EXACTLY) [SCRIPT:Script name]");

		/* Get arguments */

		for (String thisArg : arguments) {

			if (thisArg.toUpperCase().equals("FINISHED")
					|| thisArg.toUpperCase().equals("FAILED")
					|| thisArg.toUpperCase().equals("STEP")) {
				
				scriptCheck = ScriptCheck.valueOf(thisArg.toUpperCase());
				aH.echoDebug("...checking '%s'.", thisArg.toUpperCase());
			}
			
			
			else if (aH.matchesScript(thisArg)) {
				checkScript = aH.getStringModifier(thisArg);
				aH.echoDebug("...script to check is '%s'.", checkScript);
			}

			else if (thisArg.toUpperCase().matches("(?:STEP)(:)(\\d+)")) {
				step = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...step to check is '%s'.", step.toString());
			}

			else if (aH.matchesQuantity(thisArg)) {
				quantity = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...quantity to check for is '%s'.", quantity.toString());
			}

			else if (thisArg.toUpperCase().equals("EXACTLY")) {
				quantity = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...will check for EXACT quantity.");
			}

			else aH.echoError("Could not match argument '%s'!", thisArg);
		}

		
		if (scriptCheck != null) {
			
			switch (scriptCheck) {
			
			case FINISHED:

				Integer finishes = plugin.getSaves().getInt("Players." + thePlayer.getName() + "." + checkScript + "." + "Completed", 0);
				
				if (outcome == true) aH.echoDebug("...number of finishes is '%s'", finishes.toString());
				
				if (quantity == null && finishes > 0) {
					outcome = true;
					break;
				} else {
					if (exactly) {
					// check for exact number
						if (quantity == finishes) outcome = true;
						break;
					} else {
						if (finishes >= quantity) outcome = true;
					}
				}
				break;
				

			case FAILED:
			
				Integer fails = plugin.getSaves().getInt("Players." + thePlayer.getName() + "." + checkScript + "." + "Failed", 0);
				
				if (outcome == true) aH.echoDebug("...number of fails is '%s'", fails.toString());
				
				if (quantity == null && fails > 0) {
					outcome = true;
					break;
				} else {
					if (exactly) {
					// check for exact number
						if (quantity == fails) outcome = true;
						break;
					} else {
						if (fails >= quantity) outcome = true;
					}
				}
				break;

				
			case STEP:
				
				Integer currentStep = plugin.getSaves().getInt("Players." + thePlayer.getName() + "." + checkScript	+ "." + "Current Step", 0);

				if (outcome == true) aH.echoDebug("...current step is '%s'", currentStep.toString());
				
				if (step == null && currentStep > 0) {
					outcome = true;
					break;
				} else {
					if (exactly) {
					// check for exact number
						if (step == currentStep) outcome = true;
						break;
					} else {
						if (currentStep >= step) outcome = true;
					}
				}
				break;
			
			}
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}


}