package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.citizensnpcs.api.trait.trait.Owner;

import java.util.List;

public class OwnerRequirement extends AbstractRequirement {

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
        return context.getNPC().getCitizen().getTrait(Owner.class).isOwnedBy(context.getPlayer().getPlayerEntity());
    }
}
