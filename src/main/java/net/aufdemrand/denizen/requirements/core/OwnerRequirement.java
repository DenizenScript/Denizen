package net.aufdemrand.denizen.requirements.core;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.requirements.AbstractRequirement;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.entity.Player;

public class OwnerRequirement extends AbstractRequirement {


	/* OWNER

	/* Arguments: [] - Required, () - Optional 
	 * none.
	 * 
	 * Example usages:
	 * OWNER
	 */

	@Override
	public boolean check(Player thePlayer, DenizenNPC theDenizen, String theScript, String[] arguments, Boolean negativeRequirement)
			throws RequirementMissingException {

		boolean outcome = false;

		if (theDenizen.getCitizensEntity().getTrait(Owner.class).getOwner().equals(thePlayer.getName())) {
			outcome = true;
		}
				
		///////////
		if (outcome == true) aH.echoDebug("...player is owner!");
		else aH.echoDebug("...player is not owner!");
		///////////
		
		if (negativeRequirement != outcome) return true;

		return false;
	}


}