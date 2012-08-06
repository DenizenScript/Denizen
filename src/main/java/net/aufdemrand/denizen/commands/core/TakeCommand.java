package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;
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

	enum TakeType { ITEM, ITEMINHAND, MONEY }

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		TakeType takeType = null;
		int amount = 1;
		ItemStack item = null;

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				if (plugin.debugMode) 
					plugin.getLogger().info("Processing command " + theEntry.getCommand() + " argument: " + thisArgument);

				// If argument is QTY: modifier */
				if (thisArgument.matches("(?:QTY|qty)(:)(\\d+)")) {
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'Quantity'." );
					amount = Integer.valueOf(thisArgument.split(":")[1]); 
				}

				/* If the argument is ITEMINHAND */
				else if (thisArgument.toUpperCase().contains("ITEMINHAND")) {
					takeType = TakeType.ITEMINHAND;
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'Item in hand'.");
				}

				/* If the argument is MONEY */
				else if (thisArgument.toUpperCase().contains("MONEY")) {
					takeType = TakeType.MONEY;
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'Money'.");
				}

				/* If argument is and ItemID */
				else if (thisArgument.matches("\\d+")) {
					takeType = TakeType.ITEM;
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'Item ID'.");
					try {
						item = new ItemStack(Integer.valueOf(thisArgument));
					} catch (Exception e) {
						plugin.getLogger().log(Level.INFO, "...invalid Item ID.");
					}
				}

				/* If argument is ItemID:Data format */
				else if (thisArgument.matches("(\\d+)(:)(\\d+)")) {
					takeType = TakeType.ITEM;
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'specify item ID and data'.");
					try {
						item = new ItemStack(Integer.valueOf(thisArgument.split(":")[0]));
						item.setData(new MaterialData(Integer.valueOf(thisArgument.split(":")[1])));
					} catch (Exception e) {
						plugin.getLogger().log(Level.INFO, "...invalid Item ID.");
					}
				}

				/* If the argument is a Material */
				else if (thisArgument.matches("([a-zA-Z\\x5F]+)")) {
					takeType = TakeType.ITEM;
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'specify Material type'.");
					try {
						item = new ItemStack(Material.valueOf(thisArgument.toUpperCase()));
					} catch (Exception e) {
						plugin.getLogger().log(Level.INFO, "...invalid Item ID.");
					}
				}

				/* If the argument is Material:Data format */
				else if (thisArgument.matches("([a-zA-Z]+?)(:)(\\d+)")) {
					takeType = TakeType.ITEM;
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'specify Item ID and data'.");
					try {
						item = new ItemStack(Material.valueOf(thisArgument.split(":")[0].toUpperCase()));
						item.setData(new MaterialData(Integer.valueOf(thisArgument.split(":")[1])));
					} catch (Exception e) {
						plugin.getLogger().log(Level.INFO, "...Invalid Material type.");	
					}
				}

				/* Can't match to anything */
				else if (plugin.debugMode) 
					plugin.getLogger().log(Level.INFO, "...unable to match argument!");
				
			}	
		}

		
		/* Execute the command, if all required variables are filled. */
		if (takeType != null) {

			switch (takeType) {

			case MONEY:
				if (plugin.economy != null) {
					double playerBalance = plugin.economy.getBalance(theEntry.getPlayer().getName());
					double doubleAmount = Double.valueOf(amount);
					if (doubleAmount > playerBalance) { 
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...player did not have enough money to take. New amount is balance of the Player's account. To avoid this situation, use a MONEY requirement.");
						doubleAmount = playerBalance;
					}
					plugin.economy.withdrawPlayer(theEntry.getPlayer().getName(), doubleAmount);
				} else {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no economy loaded! Have you installed Vault and a compatible economy plugin?");	
				}
				break;

			case ITEMINHAND:
				int inHandAmt = theEntry.getPlayer().getItemInHand().getAmount();
				ItemStack newHandItem = new ItemStack(Material.AIR);
				if (amount > inHandAmt) {
					amount = inHandAmt;
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...player did not have enough of the item in hand, so Denizen just took as many as it could. To avoid this situation, use a HOLDING requirement.");
				}
				else { // amount is less than what's in hand, need to make a new itemstack of what's left...
					newHandItem = new ItemStack(theEntry.getPlayer().getItemInHand().getType(), inHandAmt - amount);
					newHandItem.setData(new MaterialData(theEntry.getPlayer().getItemInHand().getData().getData()));
				}

				theEntry.getPlayer().setItemInHand(newHandItem);
				break;

			case ITEM:
				item.setAmount(amount);
				if (!theEntry.getPlayer().getInventory().removeItem(item).isEmpty())
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...player did not have enough of the item specified, so Denizen just took as many as it could. To avoid this situation, use a ITEM requirement.");
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