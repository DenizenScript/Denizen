package net.aufdemrand.denizen.commands.core;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Drops an item in the world.
 * 
 * @author Jeremy Schroeder
 */

public class DropCommand extends AbstractCommand {

	/* DROP [#(:#)|MATERIAL_TYPE(:#)] (QTY:#) (BOOKMARK:LocationBookmark) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [[#](:#)|[MATERIAL_TYPE](:#)] specifies what to drop.
	 *   [#](:#) gives the item with the specified item ID. Optional
	 *     argument (:#) can specify a specific data value.
	 *   [MATERIAL_TYPE](:#) drops the item with the specified
	 *     bukkit MaterialType. Optional argument (:#) can specify
	 *     a specific data value.
	 * (QTY:#) specifies quantity. If not specified, assumed 'QTY:1'    
	 *  
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		int theAmount = 1;
		ItemStack theItem = null;
		Location theLocation = null;

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: DROP [#(:#)|MATERIAL_TYPE(:#)] (QTY:#) (BOOKMARK:LocationBookmark)");

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			// If argument is QTY: modifier
			if (aH.matchesQuantity(thisArg)) {
				theAmount = aH.getIntegerModifier(thisArg); 
				aH.echoDebug("...drop quantity now '%s'.", thisArg);
			}

			// If argument is a BOOKMARK modifier
			if (aH.matchesBookmark(thisArg)) {
				theLocation = aH.getBookmarkModifier(thisArg, theEntry.getDenizen());
				if (theLocation != null)
					aH.echoDebug("...drop location now at bookmark '%s'", thisArg);
			}

			// If argument is an Item
			else if (aH.matchesItem(thisArg)) {
				theItem = aH.getItemModifier(thisArg);
				if (theItem != null)
					aH.echoDebug("...set ItemID to '%s'.", thisArg);
			}

			// Can't match to anything
			else aH.echoError("...unable to match argument!");
		}	


		/* Execute the command, if all required variables are filled. */
		if (theItem != null) {
			theItem.setAmount(theAmount);
			if (theLocation == null) theLocation = theEntry.getDenizen().getLocation();
			theEntry.getDenizen().getWorld().dropItemNaturally(theLocation, theItem);
			return true;
		}

		return false;
	}

}