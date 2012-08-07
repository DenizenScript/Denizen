package net.aufdemrand.denizen.commands.core;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Your command! 
 * This class is a template for a Command in Denizen.
 * 
 * @author You!
 */

public class GiveCommand extends AbstractCommand {

	/* GIVE [MONEY|#(:#)|MATERIAL_TYPE(:#)] (QTY:#) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [MONEY|[#](:#)|[MATERIAL_TYPE](:#)] specifies what to give.
	 *   [MONEY] gives money using your economy.
	 *   [#](:#) gives the item with the specified item ID. Optional
	 *     argument (:#) can specify a specific data value.
	 *   [MATERIAL_TYPE](:#) gives the item with the specified
	 *     bukkit MaterialType. Optional argument (:#) can specify
	 *     a specific data value.
	 * (QTY:#) specifies quantity. If not specified, assumed 'QTY:1'    
	 *  
	 */

	enum GiveType { ITEM, MONEY }

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		GiveType giveType = null;
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

				/* If the argument is MONEY */
				else if (thisArgument.toUpperCase().contains("MONEY")) {
					giveType = GiveType.MONEY;
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...matched argument to 'Money'.");
				}

				/* If argument is and ItemID */
				else if (thisArgument.matches("\\d+")) {
					giveType = GiveType.ITEM;
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
					giveType = GiveType.ITEM;
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
					giveType = GiveType.ITEM;
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
					giveType = GiveType.ITEM;
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
		if (giveType != null) {

			switch (giveType) {

			case MONEY:
				if (plugin.economy != null) {
					double doubleAmount = Double.valueOf(amount);
					plugin.economy.depositPlayer(theEntry.getPlayer().getName(), doubleAmount);
				} else {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no economy loaded! Have you installed Vault and a compatible economy plugin?");	
				}
				break;

			case ITEM:
				item.setAmount(amount);
				HashMap<Integer, ItemStack> leftovers = theEntry.getPlayer().getInventory().addItem(item);

				if (!leftovers.isEmpty()) {
					if (plugin.debugMode)
						plugin.getLogger().log(Level.INFO, "...Player did not have enough space in their inventory, the rest of the items have been placed on the floor.");
					for (Entry<Integer, ItemStack> leftoverItem : leftovers.entrySet()) {
						theEntry.getPlayer().getWorld().dropItem(theEntry.getPlayer().getLocation(), leftoverItem.getValue());
					}
				}

				break;
			}

			return true;
		}

		/* Error processing */
		if (plugin.debugMode)
			throw new CommandException("...Usage: GIVE [MONEY|#(:#)|MATERIAL_TYPE(:#)] (QTY:#)");

		return false;
	}

}