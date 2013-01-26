package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.Depends;

public class InGroupRequirement extends AbstractRequirement{

	@Override
	public void onEnable() {
		// nothing to do here
	}

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
		boolean outcome = true;
		
		if(context.getPlayer() != null) {
			if(Depends.permissions != null) {
				for(String group: args) {
					if(Depends.permissions.playerInGroup(context.getPlayer(), group)) {
						dB.echoDebug("...player is in group: " + group);
					} else {
						outcome = false;
						dB.echoDebug("...player is not in group: " + group + "!");

						break;
					}
				}
				
				return outcome;
			}
			
			dB.echoDebug("...no permission plugin found, assume as TRUE!");
		}
		
		return outcome;
	}
}
