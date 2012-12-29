package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Drops items in a location.
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
		location = null;

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

		if (item == null) throw new InvalidArgumentsException(Messages.ERROR_INVALID_ITEM);
		if (location == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_LOCATION);
		
		if (qty != null)
			item.setAmount(qty);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		location.getWorld().dropItemNaturally(location, item);

	}
	
	
}