package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;

import java.util.List;

public class HoldingRequirement extends AbstractRequirement {

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
        boolean outcome = false;

        boolean exact = false;
        int quantity = 1;
        dItem itemToCheck = null;

        for (String thisArg : args) {
            if (aH.matchesQuantity(thisArg))
                quantity = aH.getIntegerFrom(thisArg);

            else if (aH.matchesArg("EXACT, EXACTLY, EQUALS", thisArg)) {
                exact = true;
            }

            else itemToCheck = dItem.valueOf(thisArg);
        }

        if (itemToCheck != null)
            itemToCheck.getItemStack().setAmount(quantity);

        if (exact)
            outcome = context.getPlayer().getPlayerEntity().getItemInHand().equals(itemToCheck.getItemStack());
        else
            outcome = context.getPlayer().getPlayerEntity().getItemInHand().isSimilar(itemToCheck.getItemStack());

        dB.echoDebug(context.getScriptContainer(), "Outcome: " + ((outcome) ? (exact) ? "Player is holding exact item" : "Player is holding item" : ""));

        return outcome;
    }
}
