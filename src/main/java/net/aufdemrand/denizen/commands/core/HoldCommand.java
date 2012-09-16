package net.aufdemrand.denizen.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.runnables.ThreeItemRunnable;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Your command! 
 * This class is a template for a Command in Denizen.
 * 
 * @author You!
 */

public class HoldCommand extends AbstractCommand {

	/* COMMAND_NAME [TYPICAL] (ARGUMENTS) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [TYPICAL] argument with a description if necessary.
	 * (ARGUMENTS) should be clear and concise.
	 *   
	 * Modifiers:
	 * (MODIFIER:VALUE) These are typically advanced usage arguments.
	 * (DURATION:#) They should always be optional. Use standard modifiers
	 *   already established if at all possible.
	 *   
	 * Example Usage:
	 * COMMAND_NAME VALUE
	 * COMMAND_NAME DIFFERENTVALUE OPTIONALVALUE
	 * COMMAND_NAME ANOTHERVALUE 'MODIFIER:Show one-line examples.'
	 * 
	 */
	
	private Map<String, Integer> taskMap = new ConcurrentHashMap<String, Integer>();

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		ItemStack holdItem = null;
		Integer duration = null;
		if (theEntry.arguments() == null) {
			aH.echoError("Not enough arguments! Usage: HOLD [ITEM]");
			return false;
		}

		for (String thisArg : theEntry.arguments()) {

			// Fill replaceables
			if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);
			
			if (aH.matchesItem(thisArg)) {
				holdItem = aH.getItemModifier(thisArg);
				if (holdItem != null)
					aH.echoDebug("...hold item set to '%s'.", thisArg);
			}
			else if (aH.matchesDuration(thisArg)) {
				duration = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...for a duration of '%s' seconds.", thisArg);
			}
			else aH.echoError("...unable to match '%s'!", thisArg);
		}	

		if (holdItem == null) {
			aH.echoError("Not enough arguments! Usage: HOLD [ITEM]");
			return false;
		}
		
		if (!(theEntry.getDenizen().getEntity() instanceof Player)) {
			aH.echoError("HOLD command works on 'Human' NPCs only!");
			return false;
		}
		
		if (duration != null) {

			ItemStack heldItem = ((Player) theEntry.getDenizen()).getInventory().getItemInHand();
			int newItemId = holdItem.getTypeId(); 
			if (taskMap.containsKey(theEntry.getDenizen().getName())) {
				try {
					plugin.getServer().getScheduler().cancelTask(taskMap.get(theEntry.getPlayer().getName()));
				} catch (Exception e) { }
			}
			aH.echoDebug("Setting delayed task: RESET HOLD ITEM");
			taskMap.put(theEntry.getPlayer().getName(), plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, 
					new ThreeItemRunnable<ItemStack, Integer, DenizenNPC>(heldItem, newItemId, theEntry.getDenizen()) {
				@Override
				public void run(ItemStack resetItem, Integer checkItem, DenizenNPC theDenizen) { 
					aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: RESET HELD ITEM for " + theDenizen.getName() + ".");
					if (((Player) theDenizen.getEntity()).getInventory().getItemInHand().getTypeId() == checkItem)
						((Player) theDenizen.getEntity()).setItemInHand(resetItem);
				}
			}, duration * 20));
		}
		
		((Player) theEntry.getDenizen().getEntity()).setItemInHand(holdItem);
		
		return true;
	}


}