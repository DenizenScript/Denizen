package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;

import java.util.List;

public class SunnyRequirement extends AbstractRequirement {

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
        return !context.getPlayer().getPlayerEntity().getWorld().hasStorm();
    }
}
