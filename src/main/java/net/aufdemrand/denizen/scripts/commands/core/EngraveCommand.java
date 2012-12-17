package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;
import net.aufdemrand.denizen.utilities.nbt.NBTItem;
import net.citizensnpcs.util.Messaging;

/**
 * Engraves an item. Engraved items are bound to their engraver and cannot be picked
 * up by other Players.
 * 
 * @author Jeremy Schroeder
 */

public class EngraveCommand extends AbstractCommand implements Listener {

	/* ENGRAVE (REMOVE|ADD)

	/* Arguments: [] - Required, () - Optional 
	 * 
	 * Example Usage:
	 */

	private enum EngraveAction { ADD, REMOVE, REMOVEALL }
	
	private String playerName;
	private EngraveAction action;
	private ItemStack item;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		item = null;
		playerName = null;
		if (scriptEntry.getOfflinePlayer() != null)
			playerName = scriptEntry.getOfflinePlayer().getName();
		else if (scriptEntry.getPlayer() != null) {
			playerName = scriptEntry.getPlayer().getName();
			item = scriptEntry.getPlayer().getItemInHand();
		}
		action = EngraveAction.ADD;
		
		// Parse the arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesArg("ADD, REMOVE, SET", arg)) {
				action = EngraveAction.valueOf(arg.toUpperCase());
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);
				continue;

			}	else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
	
		if (item == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ITEM");
		if (playerName == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		if (action == EngraveAction.REMOVE)
			NBTItem.removeEngraving(item, playerName);
		else if (action == EngraveAction.ADD)
			NBTItem.addEngraving(item, playerName);
	}

    @Override
    public void onEnable() {
    	denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void checkOwner(PlayerPickupItemEvent event) {
    	if (NBTItem.hasEngraving(event.getItem().getItemStack()) 
    			&& !NBTItem.getEngravings(event.getItem().getItemStack()).contains(event.getPlayer().getName())) {
    		if (event.getPlayer().isSneaking()) Messaging.send(event.getPlayer(), "That " + event.getItem().getType() + " does not belong to you!");
    		event.setCancelled(true);
    	}
    }
    
    

}