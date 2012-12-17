package net.aufdemrand.denizen.listeners.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.listeners.core.ItemListenerType.ItemType;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class ItemListenerInstance extends AbstractListener implements Listener {

	ItemType type;
	List<String> items;
	int quantity = 0;
	
	int currentItems = 0;
	
	@Override
	public void onBuild(List<String> args) {
		
		for (String arg : args) {
			if (aH.matchesValueArg("TYPE", arg, ArgumentType.Custom)) {
				try { 
					this.type = ItemType.valueOf(arg); 
					dB.echoDebug(Messages.DEBUG_SET_TYPE, this.type.name());
				} catch (Exception e) { }

			} else if (aH.matchesQuantity(arg)) {
				this.quantity = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(quantity));

			} else if (aH.matchesValueArg("ITEMS", arg, ArgumentType.Custom)) {
				items = aH.getListFrom(arg);
				dB.echoDebug("...set ITEMS.");
			}

			if (items.isEmpty()) {
				dB.echoError("Missing ITEMS argument!");
				cancel();
			}
			
			if (type == null) {
				dB.echoError("Missing TYPE argument! Valid: CRAFT, SMELT, FISH");
				cancel();
			}
			
			denizen.getServer().getPluginManager().registerEvents((Listener) this, denizen);
		}
	}

	List<Integer> itemsCrafted = new ArrayList<Integer>();
	@EventHandler
	public void listenCraft(CraftItemEvent event) {
		if (type == ItemType.CRAFT) {
			if (event.getWhoClicked() == player) {
				if (items.contains(event.getCurrentItem().getType().toString()) 
						|| items.contains(String.valueOf(event.getCurrentItem().getTypeId()))) {
					if (itemsCrafted.contains(event.getCurrentItem().getTypeId()))
						return;
					else itemsCrafted.add(event.getCurrentItem().getTypeId());

					currentItems++;
					dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " crafted a " + event.getCurrentItem().getType().toString() + ".");
					check();
				}
			}
		}
	}

	List<Integer> itemsSmelted = new ArrayList<Integer>();
	@EventHandler
	public void listenSmelt(FurnaceSmeltEvent event) {
		if (type == ItemType.SMELT) {
			InventoryClickEvent e = (InventoryClickEvent) player;
			if (event.getBlock() == e.getCurrentItem()) {
				if (items.contains(event.getBlock().getType().toString()) 
						|| items.contains(String.valueOf(event.getBlock().getTypeId()))) {
					if (itemsSmelted.contains(event.getBlock().getTypeId()))
						return;
					else itemsSmelted.add(event.getBlock().getTypeId());

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
				if (items.contains(event.getCaught().getType().toString())) {
					if (itemsFished.contains(event.getCaught().getType()))
						return;
					else itemsFished.add(event.getCaught().getType());

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
		} catch (Exception e) {
			dB.echoError("Unable to save ITEM listener for '%s'!", player.getName());
		}
	}

	@Override
	public void onLoad() {
		try {
			type = ItemType.valueOf((String) get("Type"));
			items = (List<String>) (get("Items"));
			quantity = (Integer) get("Quantity Needed");
			currentItems = (Integer) get("Quantity Done");
		} catch (Exception e) { 
			dB.echoError("Unable to load ITEM listener for '%s'!", player.getName());
			cancel();
		}
	}

	@Override
	public void onFinish() {

	}

	public void check() {
		if (quantity >= currentItems) {
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
		// TODO Auto-generated method stub
	}

	@Override
	public void deconstructed() {
		CraftItemEvent.getHandlerList().unregister(this);
		FurnaceSmeltEvent.getHandlerList().unregister(this);
		InventoryClickEvent.getHandlerList().unregister(this);
	}

}
