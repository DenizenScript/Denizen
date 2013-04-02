package net.aufdemrand.denizen.listeners.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.listeners.core.ItemListenerType.ItemType;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.depends.WorldGuardUtilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListenerInstance extends AbstractListener implements Listener {

	ItemType type = null;
	List<String> items = new ArrayList<String>();
	int quantity = 0;
	int currentItems = 0;
	Server server = Bukkit.getServer();;
	String region = null;
	
	@Override
	public void onBuild(List<String> args) {
		for (String arg : args) {
			if (aH.matchesValueArg ("TYPE", arg, ArgumentType.Custom)) {
				try { 
					this.type = ItemType.valueOf(aH.getStringFrom(arg).toUpperCase()); 
					dB.echoDebug(Messages.DEBUG_SET_TYPE, this.type.name());
				} catch (Exception e) { dB.echoError("Invalid ItemType!"); }
			}
			
			else if (aH.matchesQuantity(arg)) {
				this.quantity = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(quantity));
			}
			
			else if (aH.matchesValueArg("ITEMS, ITEM", arg, ArgumentType.Custom)) {
				for (String thisItem : aH.getListFrom(arg.toUpperCase()))
					if (server.getRecipesFor(new ItemStack(Material.matchMaterial(thisItem))) != null) {
						items.add(thisItem);
					} else dB.echoError("..." + thisItem + " is not a craftable item");
				dB.echoDebug("...set ITEMS.: " + Arrays.toString(items.toArray()));
			} else if (aH.matchesValueArg("REGION", arg, ArgumentType.String)) {
				region = aH.getStringFrom(arg);
				dB.echoDebug("...region set: " + region);
			}
		}
		
		if (items.isEmpty()) {
			dB.echoError("Missing ITEMS argument!");
			cancel();
		}
		
		if (type == null) {
			dB.echoError("Missing TYPE argument! Valid: CRAFT, SMELT, FISH");
			cancel();
		}
	}
	
	public void increment(String object, int amount)
	{
		currentItems = currentItems + amount;
		dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " " +
		type.toString().toLowerCase() + "ed " + amount + " " + object + ".");
		check();
	}

	@EventHandler
	public void listenItem(InventoryClickEvent event)
	{
		// Proceed if the slot clicked is a RESULT slot and the player is the right one
		if (event.getSlotType().toString() == "RESULT"
			&& event.getWhoClicked() == player)
		{	
			// Put the type of this inventory in a string and check if it matches the
			// listener's type
			String inventoryType = event.getInventory().getType().toString();
			if (
				   (type == ItemType.CRAFT && (inventoryType == "CRAFTING" || inventoryType == "WORKBENCH"))
				|| (type == ItemType.SMELT && inventoryType == "FURNACE")
			   )
				
			{
				if (region != null)
					if (!WorldGuardUtilities.checkPlayerWGRegion(player, region)) return;
				
				// Get the item in the result slot as an ItemStack
				final ItemStack item = new ItemStack(event.getCurrentItem());
				
				if (event.isShiftClick())
				{
					// Save the quantity of items of this type that the player had
					// before the event took place
					final int initialQty = Utilities.countItems(item, player.getInventory());
					
					// Run a task 1 tick later, after the event has occurred, and
					// see how many items of this type the player has then in the
					// inventory
					Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
		        	new Runnable()
		        	{
						@Override
		                public void run()
						{
							int newQty = Utilities.countItems(item, player.getInventory());
							int difference = newQty - initialQty;
		                            	
							// If any items were obtained (i.e. if shift click was
							// used with the player's inventory not being full),
							// increase the number of current items
							if (difference > 0)
							{
								increment(item.getType().toString(), difference);
							}
		                            	
		                }
		            }, 1);
				}
				else
				{
					// If shift click was not used, simply increase the current items
					// by the quantity of the item in the result slot
					increment(item.getType().toString(), item.getAmount());
				}
				
			}
		}
	}

	@EventHandler
	public void listenFish(PlayerFishEvent event)
	{
		if (type == ItemType.FISH)
		{
			if (event.getPlayer() == player)
			{

				if (region != null) 
					if (!WorldGuardUtilities.checkPlayerWGRegion(player, region)) return;
				
				if (event.getState().toString() == "CAUGHT_FISH")
				{
					increment("FISH", 1);
				}
			}
		}
	}
	
	@Override
	public void onSave() {
		try {
			store("Type", type.name());
			store("Items", this.items);
			store("Quantity Needed", this.quantity);
			store("Quantity Done", this.currentItems);
			store("Region", region);
		} catch (Exception e) {
			dB.echoError("Unable to save ITEM listener for '%s'!", player.getName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onLoad() {
		try {
			type = ItemType.valueOf((String) get("Type"));
			items = (List<String>) (get("Items"));
			quantity = (Integer) get("Quantity Needed");
			currentItems = (Integer) get("Quantity Done");
			region = (String) get("Region");
		} catch (Exception e) { 
			dB.echoError("Unable to load ITEM listener for '%s'!", player.getName());
			cancel();
		}
	}

	@Override
	public void onFinish() {

	}

	public void check() {
		if (currentItems >= quantity) {
			InventoryClickEvent.getHandlerList().unregister(this);
			PlayerFishEvent.getHandlerList().unregister(this);
			finish();
		}
	}
	
	@Override
	public void onCancel() {
		
	}

	@Override
	public String report() {
		return player.getName() + " current has quest listener '" + listenerId 
				+ "' active and must " + type.name() + " " + Arrays.toString(items.toArray())
				+ " '(s). Current progress '" + currentItems + "/" + quantity + "'.";
	}

	@Override
	public void constructed() {
		denizen.getServer().getPluginManager().registerEvents((Listener) this, denizen);
	}

	@Override
	public void deconstructed() {
		InventoryClickEvent.getHandlerList().unregister(this);
	}

}
