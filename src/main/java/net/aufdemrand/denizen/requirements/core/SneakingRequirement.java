package net.aufdemrand.denizen.requirements.core;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.requirements.AbstractRequirement;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.entity.Player;

public class SneakingRequirement extends AbstractRequirement {


	/* SNEAKING

	/* Arguments: [] - Required, () - Optional 
	 * none.
	 * 
	 * Example usages:
	 * SNEAKING
	 */

	@Override
	public boolean check(Player thePlayer, DenizenNPC theDenizen, String theScript, String[] arguments, Boolean negativeRequirement)
			throws RequirementMissingException {

		boolean outcome = false;

		if (thePlayer.isSneaking()) {
			outcome = true;
		}
				
		///////////
		if (outcome == true) aH.echoDebug("...player is sneaking!");
		else aH.echoDebug("...player is not sneaking!");
		///////////
		
		if (negativeRequirement != outcome) return true;

		return false;
	}


}