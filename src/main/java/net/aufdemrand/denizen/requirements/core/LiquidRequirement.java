package net.aufdemrand.denizen.requirements.core;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.requirements.AbstractRequirement;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class LiquidRequirement extends AbstractRequirement {


	/* POWERED [BOOKMARK:block]

	/* Arguments: [] - Required, () - Optional 
	 * [BOOKMARK:block] block bookmark to check if liquid.
	 * 
	 * Example usages:
	 * ISLIQUID 'BOOKMARK:block'
	 */

	@Override
	public boolean check(Player thePlayer, DenizenNPC theDenizen, String theScript, String[] arguments, Boolean negativeRequirement)
			throws RequirementMissingException {

		boolean outcome = false;
		Block blockToCheck = null;

		if (arguments == null)
			throw new RequirementMissingException("Must provide a BOOKMARK:block of the block to be checked!");

		/* Get arguments */

		for (String thisArg : arguments) {

			if (aH.matchesBookmark(thisArg)) {
				blockToCheck = aH.getBlockBookmarkModifier(thisArg, theDenizen).getBlock();
				if (blockToCheck != null)
					aH.echoDebug("...block to check is type '%s'.", blockToCheck.getType().toString());
			}

			else aH.echoError("Could not match argument '%s'!", thisArg);
		}
		

		if (blockToCheck.isLiquid()) outcome = true;
		
		///////////
		if (outcome == true) aH.echoDebug("...block is liquid!");
		else aH.echoDebug("...block is not liquid!");
		///////////
		
		if (negativeRequirement != outcome) return true;

		return false;
	}


}