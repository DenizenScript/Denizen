package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;
import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Switches a button or lever.
 * 
 * @author Jeremy Schroeder
 */

public class SwitchCommand extends AbstractCommand {

	/* SWITCH [Location_Bookmark|'Denizen Name:Location_Bookmark'] */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [Location_Bookmark|'Denizen Name:Location_Bookmark']
	 *   specifies the location of the switch
	 *   
	 * Modifiers:
	 * (DURATION:#) Reverts to the previous head position after # amount of seconds.
	 * 
	 * Example Usage:
	 * SWITCH Lever_1
	 * SWITCH 'Gatekeeper:Gate' 'DURATION:15' 
	 * SWITCH Button_3 'DURATION:4'
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		Location interactLocation = null;
		Integer duration = null;

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				if (plugin.debugMode) plugin.getLogger().info("Processing command " + theEntry.getCommand() + " argument: " + thisArgument);

				/* Set a duration */
				else if (thisArgument.toUpperCase().contains("DURATION:"))
					if (thisArgument.split(":")[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...matched DURATION modifier.");
						duration = Integer.valueOf(thisArgument.split(":")[1]);
					}

				// If argument is a valid bookmark, set location.
					else if (plugin.bookmarks.exists(theEntry.getDenizen(), thisArgument)) {
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...matched bookmark '" + thisArgument + "'.");
						interactLocation = plugin.bookmarks.get(theEntry.getDenizen(), thisArgument, BookmarkType.LOCATION);
					} else if (thisArgument.split(":").length == 2) {
						if (plugin.bookmarks.exists(thisArgument.split(":")[0], thisArgument.split(":")[1]))
							if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...matched bookmark '" + thisArgument.split(":")[0] + "'.");
						interactLocation = plugin.bookmarks.get(thisArgument.split(":")[0], thisArgument.split(":")[1], BookmarkType.LOCATION);
					}			

					else {
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...unable to match argument!");
					}

			}	
		}

		/* Execute the command. */

		if (interactLocation != null) {
			if (interactLocation.getBlock().getType() == Material.LEVER) {
				World theWorld = interactLocation.getWorld();
				net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null);
				return true;
			}

			else if (interactLocation.getBlock().getType() == Material.STONE_BUTTON) {
				World theWorld = interactLocation.getWorld();
				net.minecraft.server.Block.STONE_BUTTON.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null);
				return true;
			}

			else if (interactLocation.getBlock().getType() == Material.STONE_PLATE) {
				World theWorld = interactLocation.getWorld();
				net.minecraft.server.Block.STONE_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null);
				return true;
			}

			else if (interactLocation.getBlock().getType() == Material.WOOD_PLATE) {
				World theWorld = interactLocation.getWorld();
				net.minecraft.server.Block.WOOD_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null);
				return true;
			}

			else {
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...unusable block at this location! Found " + interactLocation.getBlock().getType().name() + ".");			
			}
		}

		
		/* Make delayed task to reset step if duration is set */
		if (duration != null) {

			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, 
					new SwitchCommandRunnable<Location>(interactLocation) {

				@Override
				public void run(Location interactLocation) { 

					if (interactLocation != null) {
						if (interactLocation.getBlock().getType() == Material.LEVER) {
							World theWorld = interactLocation.getWorld();
							net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null);
							return true;
						}

						else if (interactLocation.getBlock().getType() == Material.STONE_BUTTON) {
							World theWorld = interactLocation.getWorld();
							net.minecraft.server.Block.STONE_BUTTON.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null);
							return true;
						}

						else if (interactLocation.getBlock().getType() == Material.STONE_PLATE) {
							World theWorld = interactLocation.getWorld();
							net.minecraft.server.Block.STONE_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null);
							return true;
						}

						else if (interactLocation.getBlock().getType() == Material.WOOD_PLATE) {
							World theWorld = interactLocation.getWorld();
							net.minecraft.server.Block.WOOD_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null);
							return true;
						}

						else {
							if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...unusable block at this location! Found " + interactLocation.getBlock().getType().name() + ".");			
						}
					
					}
			}, duration * 20);
		}



		return false;
	}


}