package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Location;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Strikes the player (or NPC) with lightning.
 * 
 * @author Jeremy Schroeder
 */

public class StrikeCommand extends AbstractCommand {

	/* STRIKE (DENIZEN|[Location Bookmark]|'[Denizen Name]:[Location Bookmark]') */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * (DENIZEN) will strike the Denizen instead of the Player.
	 *   To strike the player, simply leave this argument out.
	 * ([Location Bookmark]|'[Denizen Name]:[Location Bookmark]')
	 *   to specify a specific location to strike.
	 *   
	 * Modifiers:
	 * (NODAMAGE) Makes the lightning non-lethal. No damage occured.
	 * (NPCID:#) When used in conjunction with the DENIZEN argument,
	 *   it will strike the specified Citizen. Note: Can be another
	 *   Denizen as well.
	 *   
	 * Example Usage:
	 * STRIKE
	 * STRIKE DENIZEN 
	 * STRIKE NODAMAGE
	 * STRIKE 'NPCID:6' DENIZEN
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		Boolean isLethal = true;
		Location strikeLocation = null;

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				// If argument is a modifier.
				if (thisArgument.toUpperCase().equals("DENIZEN")) {
					aH.echoDebug("...matched DENIZEN.");
					strikeLocation = theEntry.getDenizen().getLocation();
				}

				// If argument is a modifier.
				else if (thisArgument.toUpperCase().equals("NODAMAGE")) {
					aH.echoDebug("...strike is now non-lethal.");
					isLethal = false;
				}

				// If argument is a NPCID modifier...
				else if (aH.matchesNPCID(thisArgument)) {
					strikeLocation = aH.getNPCIDModifier(thisArgument).getLocation();
					if (strikeLocation != null)
						aH.echoDebug("...striking '%s'", thisArgument);
				}

				// If argument is a BOOKMARK modifier
				else if (aH.matchesBookmark(thisArgument)) {
					strikeLocation = aH.getBookmarkModifier(thisArgument, theEntry.getDenizen());
					if (strikeLocation != null)
						aH.echoDebug("...strike location now at bookmark '%s'", thisArgument);

				}		

				else aH.echoError("...unable to match argument!");
			}

		}	


		if (strikeLocation == null) strikeLocation = theEntry.getPlayer().getLocation();

		/* Execute the command. */

		// Striking Denizen..
		if (isLethal) strikeLocation.getWorld().strikeLightning(strikeLocation);
		else strikeLocation.getWorld().strikeLightningEffect(strikeLocation);

		return true;
	}

}