package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;

public class PoweredRequirement extends AbstractRequirement{

	@Override
	public void onEnable() {
		//nothing to do here
	}

	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
		boolean outcome = false;
		Block blockToCheck = null;

//	    HOW DO YOU WANT TO HANDLE NO ARGS IN REQUIREMENTS?
//		if (arguments == null)
//			throw new RequirementMissingException("Must provide a BOOKMARK:block of the block to be checked!");

		/* Get arguments */

		for (String thisArg : args) {

			if (aH.matchesLocation(thisArg)) {
				blockToCheck = aH.getLocationFrom(thisArg).getBlock();
				if (blockToCheck != null)
					dB.echoDebug("...block to check is type '%s'.", blockToCheck.getType().toString());
			}

			else dB.echoError("Could not match argument '%s'!", thisArg);
		}

		if (blockToCheck.isBlockPowered()) outcome = true;
		else if (blockToCheck.isBlockIndirectlyPowered()) outcome = true;

		if (outcome == true) dB.echoDebug("...block is powered!");
		else dB.echoDebug("...block is not powered!");

		return outcome;
	}

}
