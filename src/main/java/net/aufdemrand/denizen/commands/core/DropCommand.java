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

public class DropCommand extends AbstractCommand {

	/* DROP [#(:#)|MATERIAL_TYPE(:#)] (QTY:#) */

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

				/* If argument is and ItemID */
				else if (thisArgument.matches("\\d+")) {
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
		if (item != null) {
			item.setAmount(amount);
			theEntry.getDenizen().getWorld().dropItemNaturally(theEntry.getDenizen().getLocation(), item);
			return true;
		}

		/* Error processing */
		if (plugin.debugMode)
			throw new CommandException("...Usage: DROP [#(:#)|MATERIAL_TYPE(:#)] (QTY:#)");

		return false;
	}

}