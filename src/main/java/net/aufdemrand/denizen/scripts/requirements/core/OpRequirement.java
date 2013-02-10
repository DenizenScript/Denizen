package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.List;

public class OpRequirement extends AbstractRequirement{

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

		boolean outcome = false;

		if (context.getPlayer() != null && context.getPlayer().isOp()) {
			outcome = true;
		}

		if (outcome == true) dB.echoDebug("...player is op!");
		else dB.echoDebug("...player is not op!");
		
		return outcome;
	}
}
