package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;

public class LiquidRequirement extends AbstractRequirement{

	@Override
	public void onEnable() {
		// nothing to do here
	}

	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
		boolean outcome = false;
		Block blockToCheck = null;

//		if (args == null)
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

		if (blockToCheck.isLiquid()) outcome = true;

		if (outcome == true) dB.echoDebug("...block is liquid!");
		else dB.echoDebug("...block is not liquid!");

		return outcome;
	}

}
