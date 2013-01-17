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
	public void onEnable() {
		// nothing to do here
	}
	
	private int quantity;
    private ItemStack itemToCheck;

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
		
		boolean outcome = false;
		quantity = 1;
		itemToCheck = null;
		
		for (String thisArg : args) {
			if (aH.matchesQuantity(thisArg)){
				quantity = aH.getIntegerFrom(thisArg);
				dB.echoDebug("...quantity set to: " + quantity);
			} else {
				itemToCheck = aH.getItemFrom(thisArg);
				dB.echoDebug("...item set to: " + itemToCheck);
			}
		} 
		
		if (quantity > 1){
			itemToCheck.setAmount(quantity);
		}
		
		if (context.getPlayer().getItemInHand().equals(itemToCheck)){
			outcome = true;
			dB.echoDebug("...player is holding item");
		}
		
		return outcome;
	}
}
