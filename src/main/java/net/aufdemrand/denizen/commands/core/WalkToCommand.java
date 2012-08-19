package net.aufdemrand.denizen.commands.core;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.trait.waypoint.Waypoints;

/**
 * Your command! 
 * This class is a template for a Command in Denizen.
 * 
 * @author You!
 */

public class WalkToCommand extends AbstractCommand {

	/* WALKTO [BOOKMARK:locationbookmark]|(PLAYER)'  */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 *   
	 * Example Usage:
	 * 
	 */
	private Map<DenizenNPC, Location> returns = new  HashMap<DenizenNPC, Location>();

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 
		Location walkLocation = null;
		boolean returning = false;


		if (theEntry.getCommand().equalsIgnoreCase("return")) {
			if (returns.containsKey(theEntry.getDenizen())){
				walkLocation = returns.get(theEntry.getDenizen());
				returning = true;
			}
			else
			{
				aH.echoDebug("Return location not found for " + theEntry.getDenizen().getName());
				return false;
			}
		}

		else {
			if (theEntry.arguments() == null)
				throw new CommandException("...not enough arguments for " + theEntry.getCommand());

			/* Match arguments to expected variables */
			for (String thisArg : theEntry.arguments()) {

				// If argument is a modifier.				

				if(thisArg.equalsIgnoreCase("PLAYER")){
					if (theEntry.getPlayer() == null) return false;

					walkLocation = theEntry.getPlayer().getLocation();
					org.bukkit.util.Vector victor = walkLocation.getDirection();
					walkLocation.subtract(victor);
					aH.echoDebug("...walk location now at '%s'.", theEntry.getPlayer().getName());

				}

				// If argument is a BOOKMARK modifier
				if (aH.matchesBookmark(thisArg)) {
					walkLocation = aH.getBookmarkModifier(thisArg, theEntry.getDenizen());
					if (walkLocation != null)
						aH.echoDebug("...walk location now at '%s'.", thisArg);
				}

				else aH.echoError("...unable to match '%s'!", thisArg);
			}	
		}

		/* Execute the command, if all required variables are filled. */
		if (walkLocation != null) {

			double dist = theEntry.getDenizen().getLocation().distance(walkLocation);
			if (theEntry.getDenizen().getNavigator().getPathfindingRange() < dist ) theEntry.getDenizen().getNavigator().setPathfindingRange((float) (dist + 3)); 
			theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(true);
			theEntry.getDenizen().getNavigator().cancelNavigation();
			theEntry.getDenizen().getCitizensEntity().getNavigator().setTarget(walkLocation);
			if (returning) theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(false);
			else returns.put(theEntry.getDenizen(), walkLocation);
			return true;

		}

		return false;
	}


}