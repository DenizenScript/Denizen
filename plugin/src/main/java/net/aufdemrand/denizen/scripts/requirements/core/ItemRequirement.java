package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;

import java.util.List;

public class ItemRequirement extends AbstractRequirement {

    @Override
    public boolean check(RequirementsContext context, List<String> args)
            throws RequirementCheckException {

        dItem contains = null;
        int quantity = 1;

        for (aH.Argument arg : aH.interpret(args)) {

            if (contains == null
                    && arg.matchesArgumentType(dItem.class)) {
                contains = arg.asType(dItem.class);
            }
            else if (arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                quantity = aH.getIntegerFrom(arg.getValue());
            }
        }

        if (context.getPlayer().getPlayerEntity().getInventory().containsAtLeast(contains.getItemStack(), quantity)) {
            dB.echoDebug(context.getScriptContainer(), "...player has " + contains.identify() + ".");
            return true;

        }
        else {
            dB.echoDebug(context.getScriptContainer(), "...player doesn't have " + contains.identify() + ".");
            return false;
        }
    }
}
