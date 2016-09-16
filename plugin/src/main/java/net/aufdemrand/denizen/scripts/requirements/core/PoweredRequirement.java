package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import org.bukkit.block.Block;

import java.util.List;

public class PoweredRequirement extends AbstractRequirement {

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
        boolean outcome = false;
        Block blockToCheck = null;


        for (String thisArg : args) {

            if (aH.matchesLocation(thisArg)) {
                blockToCheck = dLocation.valueOf(thisArg).getBlock();
                if (blockToCheck != null) {
                    dB.echoDebug(context.getScriptContainer(), "...block to check is type '" + blockToCheck.getType().toString() + "'");
                }
            }

            else {
                dB.echoError("Could not match argument '" + thisArg + "'");
            }
        }

        if (blockToCheck != null) {
            if (blockToCheck.isBlockPowered()) {
                outcome = true;
            }
            else if (blockToCheck.isBlockIndirectlyPowered()) {
                outcome = true;
            }
        }

        if (outcome) {
            dB.echoDebug(context.getScriptContainer(), "...block is powered!");
        }
        else {
            dB.echoDebug(context.getScriptContainer(), "...block is not powered!");
        }

        return outcome;
    }
}
