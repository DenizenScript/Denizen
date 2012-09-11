package net.aufdemrand.denizen.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.trait.Controllable;

/**
 * Strikes the player (or NPC) with lightning.
 * 
 * @author Jeremy Schroeder
 */

public class MountCommand extends AbstractCommand implements Listener {

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

	private Map<String, Controllable> mounted = new ConcurrentHashMap<String, Controllable>();
	
	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 
		
		int radius = -1;
		Location bookmark = null;
		DenizenNPC mount = null;

		if (theEntry.getCommand().equalsIgnoreCase("UNMOUNT")) {
			mounted.get(theEntry.getPlayer().getName()).toggle();
			mounted.remove(theEntry.getPlayer().getName());
			aH.echoDebug("Player removed.");
			return true;
		}
		
		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				// If argument is a NPCID modifier...
				if (aH.matchesNPCID(thisArgument)) {
					mount = aH.getNPCIDModifier(thisArgument);
					if (mount != null)
						aH.echoDebug("...mounting '%s'", thisArgument);
				}

/*				// If argument is a BOOKMARK modifier
				else if (aH.matchesBookmark(thisArgument)) {
					bookmark = aH.getBookmarkModifier(thisArgument, theEntry.getDenizen());
					if (bookmark != null)
						aH.echoDebug("...allowed radius within '%s'", thisArgument);

				}		
				
				else if (thisArgument.toUpperCase().matches("(?RADIUS|radius|Radius)(:)(//d.)")) {
					radius = aH.getIntegerModifier(thisArgument);
					aH.echoDebug("...set '%s'", thisArgument);

				}
				*/
				
				else aH.echoError("...unable to match argument!");
			}

		}	

		if (mount == null) mount = theEntry.getDenizen();
		
		Controllable mountController = mount.getCitizensEntity().getTrait(Controllable.class);
		
		mountController.onRightClick(new NPCRightClickEvent(mount.getCitizensEntity(), theEntry.getPlayer()));
		mounted.put(theEntry.getPlayer().getName(), mount.getCitizensEntity().getTrait(Controllable.class));
		
		return true;
	}

	
	
	
// 	private Map<String, Controllable> mounted = new ConcurrentHashMap<String, Controllable>();

	// @EventHandler
	//
	

	
	
}