package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Your command! 
 * This class is a template for a Command in Denizen.
 * 
 * @author You!
 */

public class TakeCommand extends AbstractCommand {

	/* TAKE [MONEY|ITEMINHAND|#(:#)|MATERIAL_TYPE(:#)] (QTY:#) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [MONEY|ITEMINHAND|[#](:#)|[MATERIAL_TYPE](:#)] specifies what to take.
	 *   [MONEY] takes money using your economy.
	 *   [ITEMINHAND] takes from the item the Player has in their hand.
	 *   [#](:#) takes the item with the specified item ID. Optional
	 *     argument (:#) can specify a specific data value.
	 *   [MATERIAL_TYPE](:#) takes the item with the specified
	 *     bukkit MaterialType. Optional argument (:#) can specify
	 *     a specific data value.
	 * (QTY:#) specifies quantity. If not specified, assumed 'QTY:1'    
	 *  
	 */

	enum TakeType { ITEM, ITEMINHAND, MONEY, EXP }

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		TakeType takeType = null;
		int theAmount = 1;
		ItemStack theItem = null;

		/* Match arguments to expected variables */
		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: TAKE [ITEM_IN_HAND|EXP|MONEY|#(:#)|MATERIAL_TYPE(:#)] (QTY:#)");

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {
			
			// Fill replaceables
			if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);
			
			// If argument is QTY: modifier
			if (aH.matchesQuantity(thisArg)) {
				theAmount = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...set quantity to '%s'.", thisArg);
			}

			// If the argument is MONEY
			else if (thisArg.toUpperCase().contains("MONEY")) {
				takeType = TakeType.MONEY;
				aH.echoDebug("...taking MONEY.");
			}

			// If the argument is XP
			else if (thisArg.toUpperCase().contains("XP")
					|| thisArg.toUpperCase().contains("EXP")) {
				takeType = TakeType.EXP;
				aH.echoDebug("...taking EXP.");
			}

			/* If the argument is ITEMINHAND */
			else if (thisArg.toUpperCase().contains("ITEMINHAND")
					|| thisArg.toUpperCase().contains("ITEM_IN_HAND")) {
				takeType = TakeType.ITEMINHAND;
				aH.echoDebug("...matched argument to 'Item in hand'.");
			}
			
			// If argument is an Item
			else if (aH.matchesItem(thisArg)) {
				theItem = aH.getItemModifier(thisArg);
				takeType = TakeType.ITEM;
				if (theItem != null)
					aH.echoDebug("...set item to be taken to '%s'.", thisArg);
			}

			/* Can't match to anything */
			else aH.echoError("...unable to match '%s'!", thisArg);

		}	


		/* Execute the command, if all required variables are filled. */
		if (takeType != null) {

			switch (takeType) {

			case MONEY:
				if (plugin.economy != null) {
					double playerBalance = plugin.economy.getBalance(theEntry.getPlayer().getName());
					double doubleAmount = Double.valueOf(theAmount);
					if (doubleAmount > playerBalance) { 
						aH.echoDebug("...player did not have enough money to take. New amount is balance of the Player's account. To avoid this situation, use a MONEY requirement.");
						doubleAmount = playerBalance;
					}
					plugin.economy.withdrawPlayer(theEntry.getPlayer().getName(), doubleAmount);
				} else {
					aH.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");	
				}
				break;

			case ITEMINHAND:
				int inHandAmt = theEntry.getPlayer().getItemInHand().getAmount();
				ItemStack newHandItem = new ItemStack(Material.AIR);
				if (theAmount > inHandAmt) {
					aH.echoDebug("...player did not have enough of the item in hand, so Denizen just took as many as it could. To avoid this situation, use a HOLDING requirement.");
					theEntry.getPlayer().setItemInHand(newHandItem);
				}
				else {

					// amount is just right!
					if (theAmount == inHandAmt) {
						theEntry.getPlayer().setItemInHand(newHandItem);
					} else {
						// amount is less than what's in hand, need to make a new itemstack of what's left...
						newHandItem = new ItemStack(theEntry.getPlayer().getItemInHand().getType(), inHandAmt - theAmount, theEntry.getPlayer().getItemInHand().getData().getData());
						theEntry.getPlayer().setItemInHand(newHandItem);
						theEntry.getPlayer().updateInventory();
					}
				}

				break;

			case ITEM:
				theItem.setAmount(theAmount);
				if (!theEntry.getPlayer().getInventory().removeItem(theItem).isEmpty())
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...player did not have enough of the item specified, so Denizen just took as many as it could. To avoid this situation, use an ITEM requirement.");
				break;
			}

			return true;
		}

		/* Error processing */
		if (plugin.debugMode)
			throw new CommandException("...Usage: TAKE [MONEY|ITEMINHAND|#(:#)|MATERIAL_TYPE(:#)] (QTY:#)");

		return false;
	}

}