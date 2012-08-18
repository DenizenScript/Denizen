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
		Boolean strikePlayer = true;
		Location strikeLocation = null;
		
		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				if (plugin.debugMode) plugin.getLogger().info("Processing command " + theEntry.getCommand() + " argument: " + thisArgument);

				// If argument is a modifier.
				if (thisArgument.toUpperCase().equals("DENIZEN")) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...matched DENIZEN.");
					strikePlayer = false;
				}

				// If argument is a modifier.
				else if (thisArgument.toUpperCase().equals("NODAMAGE")) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...matched modifier NODAMAGE.");
							isLethal = false;
				}

				// If argument is a NPCID modifier...
				if (thisArgument.toUpperCase().contains("NPCID:")) {
					try {
						if (CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1])) != null) {
							strikeLocation = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1])).getBukkitEntity().getLocation();
							strikePlayer = false;
							if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...NPCID specified.");
						}
					} catch (Throwable e) {
						throw new CommandException("NPCID specified could not be matched to a Denizen.");
					}
				}
				
				// If argument is a BOOKMARK modifier
				if (aH.matchesBookmark(thisArgument)) {
					strikeLocation = aH.getBookmarkModifier(thisArgument, theEntry.getDenizen());
					if (strikeLocation != null)
						aH.echoDebug("...strike location now at bookmark '%s'", thisArgument);
				}		

				else {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...unable to match argument!");
				}

			}	
		}

		/* Execute the command. */

		// If striking player...
		if (strikePlayer) {
			if (isLethal) theEntry.getPlayer().getWorld().strikeLightning(theEntry.getPlayer().getLocation());
			else theEntry.getPlayer().getWorld().strikeLightningEffect(theEntry.getPlayer().getLocation());
			return true;
			} 
		
		// Not striking player...
		else {
			// Striking Denizen..
			if (strikeLocation == null) {
				if (isLethal) theEntry.getDenizen().getWorld().strikeLightning(theEntry.getPlayer().getLocation());
				else theEntry.getDenizen().getWorld().strikeLightningEffect(theEntry.getPlayer().getLocation());
			} 
			// Striking Location (or specified NPCID)
			else {
				if (isLethal) strikeLocation.getWorld().strikeLightning(theEntry.getPlayer().getLocation());
				else strikeLocation.getWorld().strikeLightningEffect(theEntry.getPlayer().getLocation());
			}
			return true;
		}

	}
}