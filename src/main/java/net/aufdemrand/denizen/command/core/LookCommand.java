package net.aufdemrand.denizen.command.core;

import java.util.logging.Level;

import org.bukkit.Location;

import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.aufdemrand.denizen.command.Command;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
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
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		/* Initialize variables */ 

		Integer duration = null;
		Direction direction = null;
		Location theLocation = null;

		DenizenNPC theDenizen = theCommand.getDenizen();

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Processing command " + theCommand.getCommand() + " argument: " + thisArgument);

				// If argument is a NPCID modifier...
				if (thisArgument.toUpperCase().contains("NPCID:")) {
					try {
						if (CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1])) != null) {
							theDenizen = plugin.getDenizenNPCRegistry().getDenizen(CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1])));
							if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...NPCID specified.");
						}
					} catch (Throwable e) {
						throw new CommandException("NPCID specified could not be matched to a Denizen.");
					}
				}

				// If argument is a DURATION modifier...
				else if (thisArgument.toUpperCase().contains("DURATION:")) {
					if (thisArgument.split(":")[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
						duration = Integer.valueOf(thisArgument.split(":")[1]);
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...duration set to " + duration + " second(s).");
					}
				}

				// If argument is a valid bookmark, set theLocation.
				else if (plugin.bookmarks.exists(theCommand.getDenizen(), thisArgument)) {
					theLocation = plugin.bookmarks.get(theCommand.getDenizen(), thisArgument, BookmarkType.LOCATION);
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...found bookmark.");	
				}
				else if (thisArgument.split(":").length == 2) {
					if (plugin.bookmarks.exists(thisArgument.split(":")[0], thisArgument.split(":")[1])) {
						theLocation = plugin.bookmarks.get(thisArgument.split(":")[0], thisArgument.split(":")[1], BookmarkType.LOCATION);
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...found bookmark.");
					}
				}

				// If argument is a direction, set Direction.
				for (Direction thisDirection : Direction.values()) {
					if (thisArgument.toUpperCase().equals(thisDirection.name())) {
						direction = Direction.valueOf(thisArgument);
						if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...looking " + direction.name() + ".");
					}
				}			

			}	
		}

		if (direction != null || theLocation != null) look(theDenizen, direction, duration, theLocation);

		return true;
	}



	private void look(DenizenNPC theDenizen, Direction lookDir, Integer duration, Location lookLoc) {

		Location restoreLocation = theDenizen.getEntity().getLocation();
		DenizenNPC restoreDenizen = theDenizen;
		String lookWhere = "NOWHERE";

		if (lookDir != null) lookWhere = lookDir.name();

		if (lookWhere.equalsIgnoreCase("CLOSE")) {
			if (!theDenizen.getCitizensEntity().getTrait(LookClose.class).toggle())
				theDenizen.getCitizensEntity().getTrait(LookClose.class).toggle();
		}

		else if (lookWhere.equalsIgnoreCase("AWAY")) {
			if (theDenizen.getCitizensEntity().getTrait(LookClose.class).toggle())
				theDenizen.getCitizensEntity().getTrait(LookClose.class).toggle();
		}

		else if (lookWhere.equalsIgnoreCase("LEFT")) {
			theDenizen.getEntity().getLocation()
			.setYaw(theDenizen.getEntity().getLocation().getYaw() - 90);
		}

		else if (lookWhere.equalsIgnoreCase("RIGHT")) {
			theDenizen.getEntity().getLocation()
			.setYaw(theDenizen.getEntity().getLocation().getYaw() + 90);
		}

		else if (lookLoc != null) {
			theDenizen.getEntity().getLocation().setPitch(lookLoc.getPitch());
			theDenizen.getEntity().getLocation().setYaw(lookLoc.getYaw());
		}

		if (duration != null) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new LookCommandRunnable<DenizenNPC, Location>(restoreDenizen, restoreLocation) {
				@Override
				public void run(DenizenNPC denizen, Location location) { 
					denizen.getEntity().getLocation().setYaw(location.getYaw());
				}
			}, duration * 20);
		}


	}
}

