package net.aufdemrand.denizen.commands.core;

import org.bukkit.Location;

import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.trait.LookClose;

/**
 * Controls Denizen's heads.
 * 
 * @author Jeremy Schroeder
 *
 */

enum Direction { UP, DOWN, LEFT, RIGHT, NORTH, SOUTH, EAST, WEST, BACK, AT, CLOSE, AWAY }

public class LookCommand extends Command {

	/* LOOK [DIRECTION|LOCATION BOOKMARK|CLOSE/AWAY]*/

	/* Arguments: [] - Required, () - Optional 
	 * 
	 * Valid Directions: UP DOWN LEFT RIGHT NORTH SOUTH EAST WEST BACK AT
	 * 
	 * Modifiers:
	 * (NPCID:#) Changes the Denizen to the Citizens2 NPCID
	 * (DURATION:#) Reverts to the previous head position after # amount of seconds.
	 */

	@Override
	public boolean execute(ScriptCommand theCommand) throws CommandException {

		/* Initialize variables */ 

		Integer duration = null;
		Direction direction = null;
		Location theLocation = null;

		NPC theDenizen = theCommand.getDenizen();

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				// If argument is a NPCID modifier...
				if (thisArgument.toUpperCase().contains("NPCID:")) {
					try {
						if (CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1])) != null)
							theDenizen = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1]));	
					} catch (Throwable e) {
						throw new CommandException("NPCID specified could not be matched to a Denizen.");
					}
				}

				// If argument is a DURATION modifier...
				else if (thisArgument.toUpperCase().contains("DURATION:")) {
					if (thisArgument.split(":")[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
						duration = Integer.valueOf(thisArgument.split(":")[1]);
				}

				// If argument is a valid bookmark, set theLocation.
				else if (plugin.bookmarks.exists(theCommand.getDenizen(), thisArgument))
					theLocation = plugin.bookmarks.get(theCommand.getDenizen(), thisArgument, BookmarkType.LOCATION);	
				else if (thisArgument.split(":").length == 2) {
					if (plugin.bookmarks.exists(thisArgument.split(":")[0], thisArgument.split(":")[1]))
						theLocation = plugin.bookmarks.get(thisArgument.split(":")[0], thisArgument.split(":")[1], BookmarkType.LOCATION);
				}
				
				// If argument is a direction, set Direction.
				for (Direction thisDirection : Direction.values()) {
					if (thisArgument.toUpperCase().equals(thisDirection.name()))
						direction = Direction.valueOf(thisArgument);
				}
			
			
			}	
		}

		return true;
	}



	private void look(NPC theDenizen, String lookWhere, Integer duration) {

		final Location restoreLocation = theDenizen.getBukkitEntity().getLocation();
		final NPC restoreDenizen = theDenizen;

		if (lookWhere.equalsIgnoreCase("CLOSE")) {
			if (!theDenizen.getTrait(LookClose.class).toggle())
				theDenizen.getTrait(LookClose.class).toggle();
		}

		else if (lookWhere.equalsIgnoreCase("AWAY")) {
			if (theDenizen.getTrait(LookClose.class).toggle())
				theDenizen.getTrait(LookClose.class).toggle();
		}

		else if (lookWhere.equalsIgnoreCase("LEFT")) {
			theDenizen.getBukkitEntity().getLocation()
			.setYaw(theDenizen.getBukkitEntity().getLocation().getYaw() - 90);
		}

		else if (lookWhere.equalsIgnoreCase("RIGHT")) {
			theDenizen.getBukkitEntity().getLocation()
			.setYaw(theDenizen.getBukkitEntity().getLocation().getYaw() + 90);
		}

		else if (plugin.bookmarks.exists(theDenizen, lookWhere)) {
			Location lookLoc = plugin.bookmarks.get(theDenizen.getName(), lookWhere, BookmarkType.LOCATION);
			theDenizen.getBukkitEntity().getLocation().setPitch(lookLoc.getPitch());
			theDenizen.getBukkitEntity().getLocation().setYaw(lookLoc.getYaw());
		}

		if (duration != null) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() { 
					restoreDenizen.getBukkitEntity().getLocation().setYaw(restoreLocation.getYaw());
				}
			}, duration * 20);
		}


	}
}