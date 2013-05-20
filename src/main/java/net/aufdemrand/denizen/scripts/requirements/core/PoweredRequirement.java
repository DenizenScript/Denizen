package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.block.Block;

import java.util.List;

public class PoweredRequirement extends AbstractRequirement{

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
		boolean outcome = false;
		Block blockToCheck = null;


		for (String thisArg : args) {

			if (aH.matchesLocation(thisArg)) {
				blockToCheck = aH.getLocationFrom(thisArg).getBlock();
				if (blockToCheck != null)
					dB.echoDebug("...block to check is type '%s'.", blockToCheck.getType().toString());
			}

			else dB.echoError("Could not match argument '%s'!", thisArg);
		}

		if(blockToCheck != null) {
			if (blockToCheck.isBlockPowered()) outcome = true;
			else if (blockToCheck.isBlockIndirectlyPowered()) outcome = true;
		}
		
		if (outcome == true) dB.echoDebug("...block is powered!");
		else dB.echoDebug("...block is not powered!");

		return outcome;
	}

}
