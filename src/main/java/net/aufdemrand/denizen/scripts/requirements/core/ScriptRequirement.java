package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.List;

public class ScriptRequirement extends AbstractRequirement{

	private enum ScriptCheck { FINISHED, FAILED, STEP }

    @Override
	public void onEnable() {
		//nothing to do here
	}

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

		boolean outcome = false;

		ScriptCheck scriptCheck = null;
		Integer step = null;
		Integer quantity = 1;
		boolean exactly = false;
		String checkScript = null;

		for (String thisArg : args) {

			if (aH.matchesValueArg("FINISHED", thisArg, ArgumentType.Custom)
					|| aH.matchesValueArg("FAILED", thisArg, ArgumentType.Custom)
					|| aH.matchesValueArg("STEP", thisArg, ArgumentType.Custom)) {

				scriptCheck = ScriptCheck.valueOf(aH.getStringFrom(thisArg));
				dB.echoDebug("...checking '%s'.", aH.getStringFrom(thisArg));
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

			else if (aH.matchesValueArg("EXACTLY", thisArg, ArgumentType.Integer)) {
				//im pretty confident this was missing from the original requirement
				exactly = true;
				quantity = aH.getIntegerFrom(thisArg);
				dB.echoDebug("...will check for EXACT quantity.");
			}

			else dB.echoError("Could not match argument '%s'!", thisArg);
		}

		if (scriptCheck != null) {
			switch (scriptCheck) {

			case FINISHED:

				Integer finishes = plugin.getSaves().getInt("Players." + context.getPlayer().getName() + "." + checkScript + "." + "Completed", 0);

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

				Integer fails = plugin.getSaves().getInt("Players." + context.getPlayer().getName() + "." + checkScript + "." + "Failed", 0);

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

				Integer currentStep = plugin.getSaves().getInt("Players." + context.getPlayer().getName() + "." + checkScript + "." + "Current Step", 0);

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
