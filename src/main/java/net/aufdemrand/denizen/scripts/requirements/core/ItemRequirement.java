package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.inventory.ItemStack;

public class ItemRequirement extends AbstractRequirement {
	
	Integer quantity = 1;
	ItemStack item = null;
	ItemScriptContainer itemContainer = null;
	
	@Override
	public boolean check(RequirementsContext context, List<String> args)
			throws RequirementCheckException {
		for (String arg : args) {
			if (aH.matchesQuantity(arg)) {
				quantity = aH.getIntegerFrom(arg);
				dB.echoDebug("...QTY set: " + quantity);
				continue;
				
			} else if (aH.matchesItem(arg)) {
				if (ScriptRegistry.getScriptContainerAs(aH.getStringFrom(arg), ItemScriptContainer.class) != null) {
					item = ScriptRegistry.getScriptContainerAs(aH.getStringFrom(arg), ItemScriptContainer.class).getItemFrom(context.getPlayer(), context.getNPC()).getItemStack();
					dB.echoDebug("...ITEM set from script");
					continue;
				} else {
					item = aH.getItemFrom(arg).getItemStack();
					dB.echoDebug("...ITEM set");
					continue;
				}
			} else if (aH.matchesItem("item:" + arg)) {
				item = aH.getItemFrom("item:" + arg).getItemStack();
				dB.echoDebug("...ITEM set");
				continue;
				
			} else throw new RequirementCheckException ("Invalid argument specified!");
		}
		
		if (context.getPlayer().getInventory().containsAtLeast(item, quantity)) {
			dB.echoDebug("...player has item");
			return true;
		} else {
			dB.echoDebug("...player doesn't have item");
			return false;
		}
	}

}
