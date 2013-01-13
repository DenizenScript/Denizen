package net.aufdemrand.denizen.listeners.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.listeners.core.BlockListenerType.BlockType;
import net.aufdemrand.denizen.utilities.Depends;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

public class BlockListenerInstance extends AbstractListener implements Listener {

	BlockType type;
	List<String> blocks = new ArrayList<String>();
	Integer quantity = 1;
	String argRegion = null;

	Integer currentBlocks = 0;
	
	@Override
	public void onBuild(List<String> args) {
		for (String arg : args) {
			if (aH.matchesValueArg("TYPE", arg, ArgumentType.Custom)) {
				try { 
					this.type = BlockType.valueOf(aH.getStringFrom(arg).toUpperCase()); 
					dB.echoDebug(Messages.DEBUG_SET_TYPE, this.type.name());
				} catch (Exception e) { dB.echoError("Invalid BlockType!"); }
			}
			
			else if (aH.matchesQuantity(arg)) {
				quantity = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(quantity));
			}
			
			else if (aH.matchesArg("BLOCKS, BLOCK", arg)){
				blocks = aH.getListFrom(arg);
				dB.echoDebug("...set BLOCK(S): " + Arrays.toString(blocks.toArray()));
			}
			
			else if (aH.matchesValueArg("REGION", arg, ArgumentType.Custom)) {
					argRegion = aH.getStringFrom(arg);
					dB.echoDebug("...set REGION.");
			}
		}
		
		if (blocks.isEmpty()) {
			dB.echoError("Missing BLOCK(S) argument!");
			cancel();
			return;
		}

		if (type == null) {
			dB.echoError("Missing TYPE argument! Valid: BUILD, COLLECT, BREAK");
			cancel();
			return;
		}
	}

	@Override
	public void onSave() {
		store("Type", type.name());
		store("Blocks", this.blocks);
		store("Quantity", this.quantity);
		store("Current Blocks", this.currentBlocks);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onLoad() {
		type = BlockType.valueOf(((String) get("Type")));
		blocks = (List<String>) get("Targets");
		quantity = (Integer) get("Quantity");
		currentBlocks = (Integer) get("Current Blocks");
	}

	@Override
	public void onFinish() {
		// nothing to do here
	}

	@Override
	public void onCancel() {
		// nothing to do here
	}

	@Override
	public String report() {
		// TODO Format a report output
		return null;
	}

	@Override
	public void constructed() {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	@Override
	public void deconstructed() {
		BlockBreakEvent.getHandlerList().unregister(this);
		PlayerPickupItemEvent.getHandlerList().unregister(this);
		BlockPlaceEvent.getHandlerList().unregister(this);
	}
	
	public void check() {
		// Check current block count vs. required count; finish() if necessary.
		if (currentBlocks >= quantity) {
			finish();
		}
	}
	
	public boolean inRegion(Player thePlayer) {
		if (Depends.worldGuard == null) return false;
		boolean inRegion = false;
		ApplicableRegionSet currentRegions = Depends.worldGuard.getRegionManager(thePlayer.getWorld()).getApplicableRegions(thePlayer.getLocation());
		for(ProtectedRegion thisRegion: currentRegions){
			dB.echoDebug("...checking current player region: " + thisRegion.getId());
			if (thisRegion.getId().contains(argRegion)) {
				inRegion = true;
				dB.echoDebug("...matched region");
			} 
		}
		return inRegion;
	}

	List<Location> blocksBroken = new ArrayList<Location>();
	@EventHandler
	public void listenBreak(BlockBreakEvent event) {
		if (type == BlockType.BREAK) {
			if (event.getPlayer() == player) {
				if (blocks.contains(event.getBlock().getType().toString())
						|| blocks.contains(String.valueOf(event.getBlock().getTypeId()))) {

					if (blocksBroken.contains(event.getBlock().getLocation()))
						return;
					else blocksBroken.add(event.getBlock().getLocation());

					currentBlocks++;
					dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " broke a " + event.getBlock().getType().toString() + ".");
					check();
				}
			}
		}
	}


	List<Integer> itemsCollected = new ArrayList<Integer>();
	@EventHandler
	public void listenCollect(PlayerPickupItemEvent event) {
		if (type == BlockType.COLLECT) {
			if (event.getPlayer() == player) {
				if (blocks.contains(event.getItem().getItemStack().getType().toString())
						|| blocks.contains(String.valueOf(event.getItem().getItemStack().getTypeId()))) {

					if (itemsCollected.contains(event.getItem().getEntityId()))
						return;
					else itemsCollected.add(event.getItem().getEntityId());

					currentBlocks = currentBlocks + event.getItem().getItemStack().getAmount();
					dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " collected a " + event.getItem().getItemStack().getType().toString() + ".");
					check();

				}
			}
		}
	}

	List<Location> blocksPlaced = new ArrayList<Location>();
	@EventHandler
	public void listenPlace(BlockPlaceEvent event) {
		if (type == BlockType.BUILD) {
			if (event.getPlayer() == player) {
				if (blocks.contains(event.getBlock().getType().toString())
						|| blocks.contains(String.valueOf(event.getBlock().getTypeId()))) {
					
					if (blocksPlaced.contains(event.getBlock().getLocation()))
						return;
					else blocksPlaced.add(event.getBlock().getLocation());
					
					currentBlocks++;
					dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " placed a " + event.getBlock().getType().toString() + ".");
					check();
				}
			}
		}
	}
	
	@EventHandler
    public void listenTag(ReplaceableTagEvent event) {
		
		if (!event.matches("LISTENER")) return;
		if (!event.getType().equalsIgnoreCase(listenerId)) return;
		
		if (event.getValue().equalsIgnoreCase("region")) {
			event.setReplaced(argRegion);
		}
		
		else if (event.getValue().equalsIgnoreCase("quantity")) {
			event.setReplaced(quantity.toString());
		}
		
		else if (event.getValue().equalsIgnoreCase("currentblocks")) {
			event.setReplaced(currentBlocks.toString());
		}
		
		else if (event.getValue().equalsIgnoreCase("targets")) {
			String blockList = "";
			for (String curTar : blocks){
				blockList = blockList + curTar + ", ";
				blockList = blockList.substring(0, blockList.length() - 1);
			}
			event.setReplaced(blockList);
		}
	}

}
