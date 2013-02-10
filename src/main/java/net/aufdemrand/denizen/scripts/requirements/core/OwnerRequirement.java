package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.trait.trait.Owner;

import java.util.List;

public class OwnerRequirement extends AbstractRequirement{

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

		boolean outcome = false;

		if (context.getNPC().getTrait(Owner.class).getOwner().equalsIgnoreCase(context.getPlayer().getName())) {
			dB.echoDebug("...NPC owner: " + context.getNPC().getTrait(Owner.class).getOwner());
			outcome = true;
		}

		if (outcome == true) dB.echoDebug("...player is owner!");
		else dB.echoDebug("...player is not owner!");
		
		return outcome;
	}
}
