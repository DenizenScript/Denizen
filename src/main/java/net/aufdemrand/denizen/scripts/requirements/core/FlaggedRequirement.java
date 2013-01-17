package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;

import java.util.List;

public class FlaggedRequirement extends AbstractRequirement {

	// TODO: Finish.

	/* FLAGGED [FLAG_NAME]

	/* Arguments: [] - Required, () - Optional
	 *  
	 */
    
    private enum FlagType { GLOBAL, DENIZEN, PLAYER }

	String flagName;
	FlagType flagType;

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

	    boolean outcome = false;

    	return outcome;
	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
    }
}