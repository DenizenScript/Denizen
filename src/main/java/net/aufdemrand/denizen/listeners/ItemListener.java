package net.aufdemrand.denizen.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.commands.core.ListenCommand;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class ItemListener extends AbstractListener {
	
	enum ItemType {CRAFT, SMELT, FISH}
	
	ItemType type;
	List<String> items;
	int quantity = 0;
	String listenerId;
	
	int currentItems = 0;

	@Override
	public void build(String listenerId, Player thePlayer, String[] args, String scriptName) {
		
		this.listenerId = listenerId;
		this.thePlayer = thePlayer;
		this.scriptName = scriptName;
		
		try {
			this.type = ItemType.valueOf(args[0]);
			this.items = new ArrayList<String>(Arrays.asList(args[1].toUpperCase().split(",")));
			this.quantity = Integer.valueOf(args[2]);
			
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		} catch (Exception e) {
			aH.echoError("Unable to build ITEM listener for '%s'!", thePlayer.getName());
			e.printStackTrace();
			cancel();
		}
		
	}
	
	List<Integer> itemsCrafted = new ArrayList<Integer>();
	@EventHandler
	public void listenCraft(CraftItemEvent event) {
		if (type == ItemType.CRAFT) {
			if (event.getWhoClicked() == thePlayer) {
				if (items.contains(event.getCurrentItem().getType().toString()) 
						|| items.contains(String.valueOf(event.getCurrentItem().getTypeId()))) {
					if (itemsCrafted.contains(event.getCurrentItem().getTypeId()))
						return;
					else itemsCrafted.add(event.getCurrentItem().getTypeId());
					
					currentItems++;
					aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " crafted a " + event.getCurrentItem().getType().toString() + ".");
					complete(false);
				}
			}
		}
	}
	
	List<Integer> itemsSmelted = new ArrayList<Integer>();
	@EventHandler
	public void listenSmelt(FurnaceSmeltEvent event) {
		if (type == ItemType.SMELT) {
			InventoryClickEvent e = (InventoryClickEvent) thePlayer;
			if (event.getBlock() == e.getCurrentItem()) {
				if (items.contains(event.getBlock().getType().toString()) 
						|| items.contains(String.valueOf(event.getBlock().getTypeId()))) {
					if (itemsSmelted.contains(event.getBlock().getTypeId()))
						return;
					else itemsSmelted.add(event.getBlock().getTypeId());
					
					currentItems++;
					aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " smelted a " + event.getBlock().getType().toString() + ".");
					complete(false);
				}
			}
		}
	}
	
	List<EntityType> itemsFished = new ArrayList<EntityType>();
	@EventHandler
	public void listenFish(PlayerFishEvent event) {
		if (type == ItemType.FISH) {
			if (event.getPlayer() == thePlayer) {
				if (items.contains(event.getCaught().getType().toString())) {
					if (itemsFished.contains(event.getCaught().getType()))
						return;
					else itemsFished.add(event.getCaught().getType());
					
					currentItems++;
					aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " fished a " + event.getCaught().getType().toString() + ".");
					complete(false);
				}
			}
		}
	}

	@Override
	public void save() {
		try {
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Listen Type", "ITEM");
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Script", this.scriptName);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Type", this.type.toString());
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Items", this.items);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Quantity", this.quantity);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Current Items", this.currentItems);
		} catch (Exception e) {
			aH.echoError("Unable to save ITEM listener for '%s'!", thePlayer.getName());
		}
		
	}

	@Override
	public void load(Player thePlayer, String listenerId) {

		try {
			this.thePlayer = thePlayer;
			this.listenerId = listenerId;
			this.scriptName = plugin.getSaves().getString("Player." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Script");
			
			this.type = ItemType.valueOf(plugin.getSaves().getString("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Type"));
			this.items = plugin.getSaves().getStringList("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Items");
			this.quantity = plugin.getSaves().getInt("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Quantity");
			this.currentItems = plugin.getSaves().getInt("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Current Items");

			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		} catch (Exception e) { 
			aH.echoError("Unable to load ITEM listener for '%s'!", thePlayer.getName());
			cancel();
		}
		
	}

	@Override
	public void complete(boolean forceable) {
		if (quantity >= currentItems || forceable) {
			CraftItemEvent.getHandlerList().unregister(this);
			FurnaceSmeltEvent.getHandlerList().unregister(this);
			InventoryClickEvent.getHandlerList().unregister(this);
			PlayerFishEvent.getHandlerList().unregister(this);
			
			plugin.getCommandRegistry().getCommand(ListenCommand.class).finish(thePlayer, listenerId, scriptName, this);
		}
		
	}

	@Override
	public void cancel() {
		CraftItemEvent.getHandlerList().unregister(this);
		FurnaceSmeltEvent.getHandlerList().unregister(this);
		InventoryClickEvent.getHandlerList().unregister(this);
		plugin.getCommandRegistry().getCommand(ListenCommand.class).cancel(thePlayer, listenerId);
	}

	@Override
	public void report() {	
	}

}
