package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;

import java.util.List;

/**
 * Basic requirement for checking if player is an OP.
 *
 * @author aufdemrand
 */
public class OpRequirement extends AbstractRequirement {

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
        // Make sure player isn't null and then check op status.
        if (context.getPlayer() != null && context.getPlayer().getPlayerEntity().isOp())
        // Player is an op, return true;
        {
            return true;
        }
        // Player is not op, return false;
        return false;
    }
}
