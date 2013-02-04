package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;

import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.flags.FlagManager.Flag;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.aH;

public class FlaggedRequirement extends AbstractRequirement {

	/* FLAGGED TYPE FLAG:value
	 * Example: FLAGGED PLAYER Cookies:3
	 * 
	 * Arguments: [] - Required, () - Optional
	 */
    
    private enum Type { GLOBAL, NPC, PLAYER }

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

	    boolean outcome = false;
        String name = null;
        String value = "true";
        String index = null;
		Type type = Type.PLAYER;
		
		for(String arg: args) {
			
			if (aH.matchesArg("GLOBAL, NPC, DENIZEN, GLOBAL", arg))
				type = Type.valueOf(arg.toUpperCase().replace("DENIZEN", "NPC"));
			
            else if (arg.split(":", 2).length > 1)
            {
                String[] flagArgs = arg.split(":");
                value = flagArgs[1].toUpperCase();
                
                if (flagArgs[0].contains("["))
                {
                	value = flagArgs[0].split("\\[", 2)[0].trim();
                	index = flagArgs[0].split("\\[", 2)[0].split("\\]", 2)[0].trim();
                }
                else
                {
                	name = flagArgs[0].toUpperCase();
                }
            }
			
            else
            	name = arg.toUpperCase();
		}

		FlagManager flagMng = DenizenAPI.getCurrentInstance().flagManager();
		Flag flag = null;
        String player = context.getPlayer().toString();
		
        switch (type) {
        case NPC:
            flag = flagMng.getNPCFlag(context.getNPC().getId(), name);
            break;
        case PLAYER:
            flag = flagMng.getPlayerFlag(player, name);
            break;
        case GLOBAL:
            flag = flagMng.getGlobalFlag(name);
            break;
        }
		
        if (index == null && flag.getLast().asString() == value)
        	outcome = true;
        else if (flag.get(Integer.parseInt(index)).asString() == value)
        	outcome = true;
		
    	return outcome;
	}

}