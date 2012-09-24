package net.aufdemrand.denizen.requirements.core;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.requirements.AbstractRequirement;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.entity.Player;

public class EnchantedRequirement extends AbstractRequirement {



	private enum CheckType {ITEMINHAND}
	
	@Override
	public boolean check(Player thePlayer, DenizenNPC theDenizen, String theScript, String[] arguments, Boolean negativeRequirement)
			throws RequirementMissingException {

		boolean outcome = false;
		CheckType checkType = null;

		if (arguments == null)
			throw new RequirementMissingException("Must provide arguments!");

		/* Get arguments */

		for (String thisArg : arguments) {

			if (thisArg.toUpperCase().equals("ITEMINHAND") ||
					thisArg.toUpperCase().equals("ITEM_IN_HAND")) {
				checkType = CheckType.ITEMINHAND;
					aH.echoDebug("...will check item in hand");
			}

			else aH.echoError("Could not match argument '%s'!", thisArg);
		}
		

		if (checkType != null) {
			switch (checkType) {
			
			case ITEMINHAND:
				if (!thePlayer.getItemInHand().getEnchantments().isEmpty()) outcome = true;
				break;
				
			}
		}
		
		///////////
		if (outcome == true) aH.echoDebug("...item is enchanted.");
		else aH.echoDebug("...item is not enchanted!");
		///////////
		
		if (negativeRequirement != outcome) return true;

		return false;
	}


}