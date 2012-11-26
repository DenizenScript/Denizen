package net.aufdemrand.denizen.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.commands.core.ListenCommand;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class BlockListener extends AbstractListener {

	enum BlockType {BUILD, COLLECT, BREAK}

	BlockType type;
	List<String> blocks;
	Integer quantity;
	String listenerId;

	Integer currentBlocks = 0;

	// new String[] { blockType, blockName, blockQty }

	@Override
	public void build(String listenerId, Player thePlayer, String[] args, String scriptName) {

		this.listenerId = listenerId;
		this.thePlayer = thePlayer;
		this.scriptName = scriptName;

		try {
			this.type = BlockType.valueOf(args[0]);
			this.blocks = Arrays.asList(args[1].toUpperCase().split(","));
			this.quantity = Integer.valueOf(args[2]);

			plugin.getServer().getPluginManager().registerEvents(this, plugin);

		} catch (Exception e) { 
			aH.echoError("Unable to build BLOCK listener for '%s'!", thePlayer.getName());
			if (plugin.debugMode) e.printStackTrace();
			cancel();
		}

	}

	List<Location> blocksBroken = new ArrayList<Location>();
	@EventHandler
	public void listenBreak(BlockBreakEvent event) {
		if (type == BlockType.BREAK) {
			if (event.getPlayer() == thePlayer) {
				if (blocks.contains(event.getBlock().getType().toString())
						|| blocks.contains(String.valueOf(event.getBlock().getTypeId()))) {

					if (blocksBroken.contains(event.getBlock().getLocation()))
						return;
					else blocksBroken.add(event.getBlock().getLocation());

					currentBlocks++;
					aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " broke a " + event.getBlock().getType().toString() + ".");
					complete(false);
				}
			}
		}
	}


	List<Integer> itemsCollected = new ArrayList<Integer>();
	@EventHandler
	public void listenCollect(PlayerPickupItemEvent event) {
		if (type == BlockType.COLLECT) {
			if (event.getPlayer() == thePlayer) {
				if (blocks.contains(event.getItem().getItemStack().getType().toString())
						|| blocks.contains(String.valueOf(event.getItem().getItemStack().getTypeId()))) {

					if (itemsCollected.contains(event.getItem().getEntityId()))
						return;
					else itemsCollected.add(event.getItem().getEntityId());

					currentBlocks++;
					aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " collected a " + event.getItem().getItemStack().getType().toString() + ".");
					complete(false);

				}
			}
		}
	}

	List<Location> blocksPlaced = new ArrayList<Location>();
	@EventHandler
	public void listenPlace(BlockPlaceEvent event) {
		if (type == BlockType.BUILD) {
			if (event.getPlayer() == thePlayer) {
				if (blocks.contains(event.getBlock().getType().toString())
						|| blocks.contains(String.valueOf(event.getBlock().getTypeId()))) {
					
					if (blocksPlaced.contains(event.getBlock().getLocation()))
						return;
					else blocksPlaced.add(event.getBlock().getLocation());
					
					currentBlocks++;
					aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " placed a " + event.getBlock().getType().toString() + ".");
					complete(false);
				}
			}
		}
	}


	@Override
	public void complete(boolean forceable) {

		if (quantity == currentBlocks || forceable) {
			PlayerPickupItemEvent.getHandlerList().unregister(this);
			BlockBreakEvent.getHandlerList().unregister(this);
			BlockPlaceEvent.getHandlerList().unregister(this);
			
			// Call script
			plugin.getCommandRegistry().getCommand(ListenCommand.class).finish(thePlayer, listenerId, scriptName, this);
		}
	}


	@Override
	public void cancel() {
		PlayerPickupItemEvent.getHandlerList().unregister(this);
		BlockBreakEvent.getHandlerList().unregister(this);
		BlockPlaceEvent.getHandlerList().unregister(this);
		plugin.getCommandRegistry().getCommand(ListenCommand.class).cancel(thePlayer, listenerId);
	}


	@Override
	public void save() {

		try {
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Listen Type", "BLOCK");
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Script", this.scriptName);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Type", this.type.toString());
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Blocks", this.blocks);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Quantity", this.quantity);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Current Blocks", this.currentBlocks);

		} catch (Exception e) { 
			aH.echoError("Unable to save BLOCK listener for '%s'!", thePlayer.getName());
		}
	}


	@Override
	public void load(Player thePlayer, String listenerId) {

		try { 
			this.thePlayer = thePlayer;
			this.listenerId = listenerId;
			this.scriptName = plugin.getSaves().getString("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Script"); 

			this.type = BlockType.valueOf(plugin.getSaves().getString("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Type"));
			this.blocks = plugin.getSaves().getStringList("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Blocks");
			this.quantity = plugin.getSaves().getInt("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Quantity");
			this.currentBlocks = plugin.getSaves().getInt("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Current Blocks");

			plugin.getServer().getPluginManager().registerEvents(this, plugin);

		} catch (Exception e) { 
			aH.echoError("Unable to load BLOCK listener for '%s'!", thePlayer.getName());
			cancel();
		}
	}


	@Override
	public void report() {

	}

}
