package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
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

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: SWITCH [BOOKMARK:block] (DURATION:#)");

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			// DURATION argument
			if (aH.matchesDuration(thisArg)) {
				duration = Integer.valueOf(thisArg.split(":")[1]);
			}

			// If argument is a BOOKMARK modifier
			else if (aH.matchesBookmark(thisArg)) {
				interactLocation = aH.getBlockBookmarkModifier(thisArg, theEntry.getDenizen());
				if (interactLocation != null)	aH.echoDebug("...switch location now at bookmark '%s'", thisArg);
				else{
					aH.echoDebug("... could not find block bookmark: '%s'", thisArg);
					interactLocation = aH.getBookmarkModifier(thisArg, theEntry.getDenizen());
					if (interactLocation != null) aH.echoDebug("...Found location bookmark matching '%s' using that.", thisArg);
					// else	aH.echoDebug("... could not find any bookmark: '%s'", thisArg);
				}

			}		

			else {
				aH.echoError("...unable to match '%s'.", thisArg);
			}

		}	

		/* Execute the command. */

		if (interactLocation == null) {
			aH.echoError("No interact location specified! Must use BOOKMARK:block to specify a location.");
			return false;
		}

		if (interactLocation.getBlock().getType() == Material.LEVER) {
			World theWorld = interactLocation.getWorld();
			net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
		}

		else if (interactLocation.getBlock().getType() == Material.STONE_BUTTON) {
			World theWorld = interactLocation.getWorld();
			net.minecraft.server.Block.STONE_BUTTON.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
		}

		else if (interactLocation.getBlock().getType() == Material.STONE_PLATE) {
			World theWorld = interactLocation.getWorld();
			net.minecraft.server.Block.STONE_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
		}

		else if (interactLocation.getBlock().getType() == Material.WOOD_PLATE) {
			World theWorld = interactLocation.getWorld();
			net.minecraft.server.Block.WOOD_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
		}

		else {
			aH.echoError("Unusable block at this location! Found " + interactLocation.getBlock().getType().name() + ".");
			return false;
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
							net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
							return;
						}

						else if (interactLocation.getBlock().getType() == Material.STONE_BUTTON) {
							World theWorld = interactLocation.getWorld();
							net.minecraft.server.Block.STONE_BUTTON.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
							return;
						}

						else if (interactLocation.getBlock().getType() == Material.STONE_PLATE) {
							World theWorld = interactLocation.getWorld();
							net.minecraft.server.Block.STONE_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
							return;
						}

						else if (interactLocation.getBlock().getType() == Material.WOOD_PLATE) {
							World theWorld = interactLocation.getWorld();
							net.minecraft.server.Block.WOOD_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
							return;
						}

						else {
							if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...unusable block at this location! Found " + interactLocation.getBlock().getType().name() + ".");			
						}
					}
				}
			}, duration * 20);
		}

		return true;
	}


}