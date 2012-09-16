package net.aufdemrand.denizen.commands.core;

import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
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

	enum DropType {ITEM, EXP}

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		int theAmount = 1;
		ItemStack theItem = null;
		Location theLocation = null;
		DropType dropType = null;

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: DROP [#(:#)|MATERIAL_TYPE(:#)] (QTY:#) (BOOKMARK:LocationBookmark)");

		for (String thisArg : theEntry.arguments()) {

			// Fill replaceables
			if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);

			if (aH.matchesQuantity(thisArg)) {
				theAmount = aH.getIntegerModifier(thisArg); 
				aH.echoDebug("...drop quantity now '%s'.", thisArg);
			}

			else if (aH.matchesBookmark(thisArg)) {
				theLocation = aH.getBookmarkModifier(thisArg, theEntry.getDenizen());
				if (theLocation != null)
					aH.echoDebug("...drop location now at bookmark '%s'", thisArg);
			}

			else if (aH.matchesNPCID(thisArg)) {
				theLocation = aH.getNPCIDModifier(thisArg).getLocation();
				if (theLocation !=null)	aH.echoDebug("...now targeting '%s'.", thisArg);
			}

			else if (thisArg.toUpperCase().contains("XP")) {
				dropType = DropType.EXP;
				aH.echoDebug("...giving '%s'.", thisArg);
			}

			else if (aH.matchesItem(thisArg)) {
				theItem = aH.getItemModifier(thisArg);
				dropType = DropType.ITEM;
				if (theItem != null)
					aH.echoDebug("...set ItemID to '%s'.", thisArg);
			}

			else aH.echoError("...unable to match '%s'!", thisArg);
		}	


		// Catch TASK-type script usage.
		if (theLocation == null && theEntry.getDenizen() == null) {
			aH.echoError("Seems this was sent from a TASK-type script. Must use BOOKMARK:location or NPCID:# to specify a drop location!");
			return false;
		}


		if (theLocation == null) theLocation = theEntry.getDenizen().getLocation();
		

		if (dropType != null) {

			switch (dropType) {

			case ITEM:
				theItem.setAmount(theAmount);
				theEntry.getDenizen().getWorld().dropItemNaturally(theLocation, theItem);
				return true;

			case EXP:
				((ExperienceOrb) theEntry.getDenizen().getWorld().spawn(theLocation, ExperienceOrb.class)).setExperience(theAmount);
				return true;
			}
		}

		return false;
	}
	
}

