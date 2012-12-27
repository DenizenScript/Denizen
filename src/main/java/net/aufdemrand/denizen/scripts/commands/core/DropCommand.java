package net.aufdemrand.denizen.scripts.commands.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Drops items in a world.
 * 
 * @author Jeremy Schroeder
 */

public class DropCommand extends AbstractCommand {

	@Override
	public void onEnable() {

	}

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * 
	 * 
	 * 
	 * Example Usage:
	 * 
	 */
	
	ItemStack item;
	Location location;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		item = null;
		Integer qty = null;

		for (String arg : scriptEntry.getArguments()) {
			
			if (aH.matchesItem(arg)) {
				 item = aH.getItemFrom(arg);
				 dB.echoDebug("...set ITEM: '%s'", aH.getStringFrom(arg));
				 continue;
				
			} else if (aH.matchesQuantity(arg)) {
				 qty = aH.getIntegerFrom(arg);
				 dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(qty));
				 continue;
			
			} else if (aH.matchesLocation(arg)) {
				 location = aH.getLocationFrom(arg);
				 dB.echoDebug(Messages.DEBUG_SET_LOCATION, aH.getStringFrom(arg));
				 continue;
				 
			} else {
				
			}
		}

		if (qty != null)
			item.setAmount(qty);
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {

		location.getWorld().dropItemNaturally(location, item);

	}
	
	
}