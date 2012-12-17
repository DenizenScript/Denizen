package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class HoldingRequirement extends AbstractRequirement{

	@Override
	public void onEnable() {
		// nothing to do here
	}
	
	private int quantity;
    private Material material;
    private ItemStack itemToCheck;
    
	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
		
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
		
		if (player.getItemInHand().equals(itemToCheck)){
			outcome = true;
			dB.echoDebug("...player is holding item");
		}
		
		return outcome;
	}

}
