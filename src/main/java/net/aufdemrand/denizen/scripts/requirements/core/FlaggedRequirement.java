package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class FlaggedRequirement extends AbstractRequirement {

	/* FLAGGED [TYPE:FLAG_NAME]
	 * Example: FLAGGED PLAYER:heHazCookiez
	 * 
	 * Arguments: [] - Required, () - Optional
	 */
    
    private enum FlagType { GLOBAL, NPC, PLAYER }

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

	    boolean outcome = false;
		
		FlagType type = null;
		String flag = "";
		
		for(String arg: args) {
			String[] flagList = arg.split(":");
			
			if(flagList.length < 2) continue;
			
			String typeName = flagList[0].toUpperCase();
			
			if(typeName.startsWith("P")) // PLAYER flag
				type = FlagType.PLAYER;
			else if(typeName.startsWith("N")) // NPC flag
				type = FlagType.NPC;
			else if(typeName.startsWith("G")) // GLOBAL flag
				type = FlagType.GLOBAL;
			
			flag = arg.substring(typeName.length() + 1, arg.length());
		}

		FlagManager flagMng = DenizenAPI.getCurrentInstance().flagManager();
		
		switch(type) {
			case PLAYER:
				Player player = context.getPlayer();
				
				if(player != null)
					outcome = flagMng.getPlayerFlag(player.getName(), flag).size() > 0;
				
				break;
				
			case NPC:
				dNPC npc = context.getNPC();
				
				if(npc != null)
					outcome = flagMng.getNPCFlag(npc.getId(), flag).size() > 0;
					
				break;
				
			case GLOBAL:
				outcome = flagMng.getGlobalFlag(flag).size() > 0;
		}
		
    	return outcome;
	}

}