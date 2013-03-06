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
import net.aufdemrand.denizen.utilities.runnables.Runnable2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
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

	@EventHandler
	public void listenCraft(CraftItemEvent event) {
		if (type == ItemType.CRAFT) {
			if (event.getWhoClicked() == player) {
				
				if (region != null) 
					if (!WorldGuardUtilities.checkPlayerWGRegion(player, region)) return;
				
				if (items.contains(event.getCurrentItem().getType().toString()) 
						|| items.contains(String.valueOf(event.getCurrentItem().getTypeId()))) {
				
					// Save the item stack that results from one crafting
					ItemStack item = new ItemStack(event.getCurrentItem());
					
					// Save the quantity of items of this type that the player had
					// before the crafting took place
					int initialQty = Utilities.countItems(item, player.getInventory());
										
					// Run a task 1 tick later, after the crafting has occurred, and
					// see how many items of this type the player has then in the
					// inventory, in case shift-click was used
					
					Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
		        			new Runnable2<ItemStack, Integer>(item, initialQty) {
		                            @Override
		                            public void run(ItemStack item, Integer initialQty) {
		                            	int newQty = Utilities.countItems(item, player.getInventory());
		                            	int difference = newQty - initialQty;
		                            	
		                            	// If the difference is 0 and the player's cursor item
		                            	// is not null, that means crafting succeeded but
		                            	// shift-click was not used, so we use the default
		                            	// quantity for crafting this item
		                            	
		                            	if (difference == 0 && player.getItemOnCursor().getType().toString() != "AIR")
		                            	{
		                            		difference = item.getAmount();
		                            	}
		                            	
		                            	// If anything was crafted, increase the number of items
		                            	// crafted and check the listener
		                            	if (difference > 0)
		                            	{
		                            		currentItems = currentItems + difference;
		                            		dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " crafted "
		                            				 + difference + " of " + item.getType().toString() + ".");
		                            		check();
		                            	}
		                            	
		                            }
		                        }, 1);
				}
			}
		}
	}

	List<Integer> itemsSmelted = new ArrayList<Integer>();
	@EventHandler
	public void listenSmelt(FurnaceSmeltEvent event) {
		if (type == ItemType.SMELT) {

			if (region != null) 
				if (!WorldGuardUtilities.checkPlayerWGRegion(player, region)) return;
			
			InventoryClickEvent e = (InventoryClickEvent) player;
			if (event.getBlock() == e.getCurrentItem()) {
				if (items.contains(event.getBlock().getType().toString()) 
						|| items.contains(String.valueOf(event.getBlock().getTypeId()))) {

					itemsSmelted.add(event.getBlock().getTypeId());
					currentItems++;
					dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " smelted a " + event.getBlock().getType().toString() + ".");
					check();
				}
			}
		}
	}

	List<EntityType> itemsFished = new ArrayList<EntityType>();
	@EventHandler
	public void listenFish(PlayerFishEvent event) {
		if (type == ItemType.FISH) {
			if (event.getPlayer() == player) {

				if (region != null) 
					if (!WorldGuardUtilities.checkPlayerWGRegion(player, region)) return;
				
				if (items.contains(event.getCaught().getType().toString())) {
					
					itemsFished.add(event.getCaught().getType());
					currentItems++;
					dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " fished a " + event.getCaught().getType().toString() + ".");
					check();
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
			CraftItemEvent.getHandlerList().unregister(this);
			FurnaceSmeltEvent.getHandlerList().unregister(this);
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
		CraftItemEvent.getHandlerList().unregister(this);
		FurnaceSmeltEvent.getHandlerList().unregister(this);
		InventoryClickEvent.getHandlerList().unregister(this);
	}

}
