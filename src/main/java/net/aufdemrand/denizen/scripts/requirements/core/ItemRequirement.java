package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class ItemRequirement extends AbstractRequirement {
	
	Integer quantity = 0;
	ItemStack item = null;
	
	@Override
	public boolean check(RequirementsContext context, List<String> args)
			throws RequirementCheckException {
		for (String arg : args) {
			if (aH.matchesQuantity(arg)) {
				quantity = aH.getIntegerFrom(arg);
				dB.echoDebug("...QTY set: " + quantity);
				continue;
				
			} else if (aH.matchesItem(arg)) {
				item = aH.getItemFrom(arg);
				dB.echoDebug("...ITEM set");
				continue;
				
			} else throw new RequirementCheckException ("Invalid argument specified!");
		}
		
		if (context.getPlayer().getInventory().contains(item)) {
			dB.echoDebug("...player has item");
			return true;
		} else {
			dB.echoDebug("...player doesn't have item");
			return false;
		}
	}

}
