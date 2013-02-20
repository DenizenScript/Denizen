package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;

public class PermissionRequirement extends AbstractRequirement{

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {		
		if(context.getPlayer() != null) {			
			if(Depends.permissions != null) {
				boolean outcome = true;
				
				for(String permission: args) {
					if(Depends.permissions.has(context.getPlayer(), permission)) {
						dB.echoDebug("...player has permission: " + permission);
					} else {
						outcome = false;
						dB.echoDebug("...player does not have permission: " + permission + "!");

						break;
					}
				}
				
				return outcome;
			}
			
			dB.echoDebug("...no permission plugin found, assume as TRUE!");
		}
		
		return true;
	}
}
