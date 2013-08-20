package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import org.bukkit.World;

import java.util.List;

/**
 * Returns whether or not it's storming in the world where the player is.
 */
public class StormRequirement extends AbstractRequirement {

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
        World world = context.getPlayer().getPlayerEntity().getWorld();

        return world.hasStorm() && world.isThundering();
    }
}
