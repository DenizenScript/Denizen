package net.aufdemrand.denizen.commands.core;

import org.bukkit.Location;

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

		if (theEntry.arguments() == null)
			throw new CommandException("...not enough arguments! Usage: TELEPORT '[Location Bookmark]|'[Denizen Name]:[Location Bookmark]'");

		
		/* Match arguments to expected variables */
			for (String thisArg : theEntry.arguments()) {

				// Fill replaceables
				if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);
				
				// If argument is a modifier.
				if (thisArg.toUpperCase().contains("DENIZEN")) {
					teleportPlayer = false;
					aH.echoDebug("...now teleporting DENIZEN instead of PLAYER.", thisArg);
				}

				// If argument is a BOOKMARK modifier
				if (aH.matchesBookmark(thisArg)) {
					teleportLocation = aH.getBookmarkModifier(thisArg, theEntry.getDenizen());
					if (teleportLocation != null)
						aH.echoDebug("...teleport location now at '%s'.", thisArg);
				}
				
				else aH.echoError("...unable to match '%s'!", thisArg);

			}	

		/* Execute the command, if all required variables are filled. */
		if (teleportLocation != null) {

			if (teleportPlayer) theEntry.getPlayer().teleport(teleportLocation);
			else theEntry.getDenizen().getEntity().teleport(teleportLocation);

			return true;
		}

		return false;
	}


}