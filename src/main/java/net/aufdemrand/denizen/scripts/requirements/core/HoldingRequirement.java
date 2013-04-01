package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.arguments.Item;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HoldingRequirement extends AbstractRequirement{

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
		boolean outcome = false;
		
		boolean exact = false;
		int quantity = 1;
		Item itemToCheck = null;
		
		for (String thisArg : args) {
			if (aH.matchesQuantity(thisArg))
				quantity = aH.getIntegerFrom(thisArg);

			else if(aH.matchesArg("EXACT, EXACTLY, EQUALS", thisArg)) {
				exact = true;
			}

            else itemToCheck = aH.getItemFrom(thisArg);
		}
		
		if (itemToCheck != null)
			itemToCheck.getItemStack().setAmount(quantity);

		if (exact)
            outcome = context.getPlayer().getItemInHand().equals(itemToCheck);
		else
            outcome = context.getPlayer().getItemInHand().isSimilar(itemToCheck.getItemStack());
		
		dB.report("Outcome", (outcome) ? (exact) ? "Player is holding exact item" : "Player is holding item" : "");

		return outcome;
	}
}
