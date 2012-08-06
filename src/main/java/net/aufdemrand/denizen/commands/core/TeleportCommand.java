package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Location;

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

public class TeleportCommand extends AbstractCommand {

	/* TELEPORT '[Location Bookmark]|'[Denizen Name]:[Location Bookmark]'  */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [Location Bookmark]|'[Denizen Name]:[Location Bookmark]'
	 *   to specify the location to teleport to.
	 *   
	 * Modifiers:
	 * (DENIZEN) Teleports the Denizen instead of the Player.
	 *   
	 * Example Usage:
	 * TELEPORT SpawnLoc3
	 * TELEPORT 'Gatekeeper:GateEntry'
	 * TELEPORT DENIZEN SpawnLoc13
	 * 
	 */

	@Override

	// This is the method that is called when your command is ready to be executed.
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		Location teleportLocation = null;
		Boolean teleportPlayer = true;

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				if (plugin.debugMode) plugin.getLogger().info("Processing command " + theEntry.getCommand() + " argument: " + thisArgument);

				// If argument is a modifier.
				if (thisArgument.toUpperCase().contains("DENIZEN")) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...teleporting the DENIZEN instead of the Player.");
					teleportPlayer = false;
				}

				/* If argument is a BOOKMARK modifier */
				else if (thisArgument.matches("(?:bookmark|BOOKMARK)(:)(\\w+)(:)(\\w+)") 
						&& plugin.bookmarks.exists(thisArgument.split(":")[1], thisArgument.split(":")[2])) {
					teleportLocation = plugin.bookmarks.get(thisArgument.split(":")[1], thisArgument.split(":")[2], BookmarkType.LOCATION);
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...argument matched to 'valid bookmark location'.");
				} else if (thisArgument.matches("(?:bookmark|BOOKMARK)(:)(\\w+)") &&
						plugin.bookmarks.exists(theEntry.getDenizen(), thisArgument.split(":")[1])) {
					teleportLocation = plugin.bookmarks.get(theEntry.getDenizen(), thisArgument, BookmarkType.LOCATION);
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...argument matched to 'valid bookmark location'.");
				}
				
				else {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...unable to match argument!");
				}

			}	
		}

		/* Execute the command, if all required variables are filled. */
		if (teleportLocation != null) {

			if (teleportPlayer) theEntry.getPlayer().teleport(teleportLocation);
			else theEntry.getDenizen().getEntity().teleport(teleportLocation);

			return true;
		}

		// else...

		/* Error processing */

		if (plugin.debugMode) if (theEntry.arguments() == null)
			throw new CommandException("...not enough arguments! Usage: TELEPORT '[Location Bookmark]|'[Denizen Name]:[Location Bookmark]'");

		return false;
	}


}