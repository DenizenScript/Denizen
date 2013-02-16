package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
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
		ItemStack itemToCheck = null;
		
		for (String thisArg : args) {
			if (aH.matchesQuantity(thisArg)){
				quantity = aH.getIntegerFrom(thisArg);
				dB.echoDebug("...quantity set to: " + quantity);
			} else if(aH.matchesArg("EXACT, EXACTLY, EQUALS", thisArg)) {
				exact = true;
				dB.echoDebug("...exact item match set to TRUE");
			} else {
				itemToCheck = aH.getItemFrom(thisArg);
				dB.echoDebug("...item set to: " + itemToCheck);
			}
		} 
		
		if (itemToCheck != null && quantity > 1) {
			itemToCheck.setAmount(quantity);
		}
		
		if (exact) outcome = context.getPlayer().getItemInHand().isSimilar(itemToCheck);
		else outcome = context.getPlayer().getItemInHand().equals(itemToCheck);
		
		if(outcome) dB.echoDebug("...player is holding item");
		
		return outcome;
	}
}
