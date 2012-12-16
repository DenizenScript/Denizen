package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;

public class ScriptRequirement extends AbstractRequirement{

	private enum ScriptCheck { FINISHED, FAILED, STEP };
	
	@Override
	public void onEnable() {
		//nothing to do here
	}

	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
		boolean outcome = false;

		ScriptCheck scriptCheck = null;
		Integer step = null;
		Integer quantity = 1;
		boolean exactly = false;
		String checkScript = null;

//		if (args == null)
//			throw new RequirementMissingException("Not enough arguments! Usage... SCRIPT [FINISHED|FAILED|STEP] (STEP:#) (QTY:#) (EXACTLY) [SCRIPT:Script name]");

		/* Get arguments */

		for (String thisArg : args) {

			if (aH.matchesValueArg("FINISHED", thisArg, ArgumentType.Custom)
					|| aH.matchesValueArg("FAILED", thisArg, ArgumentType.Custom)
					|| aH.matchesValueArg("STEP", thisArg, ArgumentType.Custom)) {

				scriptCheck = ScriptCheck.valueOf(aH.getStringFrom(thisArg));
				dB.echoDebug("...checking '%s'.", thisArg.toUpperCase());
			}


			else if (aH.matchesScript(thisArg)) {
				checkScript = aH.getStringFrom(thisArg);
				dB.echoDebug("...script to check is '%s'.", checkScript);
			}

			else if (thisArg.toUpperCase().matches("(?:STEP)(:)(\\d+)")) {
				step = aH.getIntegerFrom(thisArg);
				dB.echoDebug("...step to check is '%s'.", step.toString());
			}

			else if (aH.matchesQuantity(thisArg)) {
				quantity = aH.getIntegerFrom(thisArg);
				dB.echoDebug("...quantity to check for is '%s'.", quantity.toString());
			}

			else if (thisArg.toUpperCase().equals("EXACTLY")) {
				quantity = aH.getIntegerFrom(thisArg);
				dB.echoDebug("...will check for EXACT quantity.");
			}

			else dB.echoError("Could not match argument '%s'!", thisArg);
		}


		if (scriptCheck != null) {

			switch (scriptCheck) {

			case FINISHED:

				Integer finishes = plugin.getSaves().getInt("Players." + player.getName() + "." + checkScript + "." + "Completed", 0);

				if (outcome == true) dB.echoDebug("...number of finishes is '%s'", finishes.toString());

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

				Integer fails = plugin.getSaves().getInt("Players." + player.getName() + "." + checkScript + "." + "Failed", 0);

				if (outcome == true) dB.echoDebug("...number of fails is '%s'", fails.toString());

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

				Integer currentStep = plugin.getSaves().getInt("Players." + player.getName() + "." + checkScript	+ "." + "Current Step", 0);

				if (outcome == true) dB.echoDebug("...current step is '%s'", currentStep.toString());

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

		return outcome;
	}

}
