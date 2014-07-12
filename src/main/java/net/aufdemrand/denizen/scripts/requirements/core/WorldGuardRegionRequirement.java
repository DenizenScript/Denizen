package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.List;

/**
 * Checks if Player is inside specified WorldGuard region.
 *
 * @author Mason Adkins
 */

public class WorldGuardRegionRequirement extends AbstractRequirement {

    /* INREGION [NAME:regionname]

    /* Arguments: [] - Required, () - Optional
     * [NAME:regionname] region to check if player is in.
     *
     * Example usages:
     * INREGION NAME:ilovejeebiss
     */


    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

        // TODO: Remove entirely.
        dB.echoError("Requirements are deprecated, and Denizen no longer directly links WorldGuard." +
                "Please use Depenizen and the tag l@location.in_region in an if command.");
        return false;
    }
}
