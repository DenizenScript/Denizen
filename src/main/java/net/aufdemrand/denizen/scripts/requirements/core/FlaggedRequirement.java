package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;

import org.bukkit.entity.Player;

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
	public boolean check(Player thePlayer, DenizenNPC theDenizen, String theScript, List<String> arguments) throws RequirementCheckException {

	    boolean outcome = false;


    	return outcome;
	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
    }
}