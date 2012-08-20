package net.aufdemrand.denizen.commands.core;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.util.Vector;

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
		Float Speed = null;

		for (String thisArg : theEntry.arguments()) {

			if(thisArg.toUpperCase().contains("SPEED:")){

				try {
					Speed = Float.valueOf( aH.getStringModifier(thisArg));		
					aH.echoDebug("... speed set to " + Speed);
				} catch (Exception e) {
					aH.echoDebug("... Invalid Speed!");
				}
			}
		}


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

		else if(theEntry.getCommand().equalsIgnoreCase("walkto"))  {
			walkLocation = handleWalkTo(theEntry);
		}
		else if(theEntry.getCommand().equalsIgnoreCase("walk"))  {
			walkLocation = handleWalk(theEntry);
		}

		/* Execute the command, if all required variables are filled. */
		if (walkLocation != null) {

			double dist = theEntry.getDenizen().getLocation().distance(walkLocation);
			if (theEntry.getDenizen().getNavigator().getPathfindingRange() < dist ) theEntry.getDenizen().getNavigator().setPathfindingRange((float) (dist + 3)); 
			theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(true);
			theEntry.getDenizen().getNavigator().cancelNavigation();
			if(Speed!=null) theEntry.getDenizen().getCitizensEntity().getNavigator().setSpeed(Speed);
			theEntry.getDenizen().getCitizensEntity().getNavigator().setTarget(walkLocation);
			if (returning) theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(false);
			else returns.put(theEntry.getDenizen(), walkLocation);
			return true;

		}
		else	aH.echoDebug("...No location!");
		
		
		return false;
	}

	private Location handleWalkTo(ScriptEntry theEntry){
		Location out = null;
		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			// If argument is a modifier.				

			if(thisArg.equalsIgnoreCase("PLAYER")){
				if (theEntry.getPlayer() == null) return null;

				out = theEntry.getPlayer().getLocation();
				org.bukkit.util.Vector victor = out.getDirection();
				out.subtract(victor);
				aH.echoDebug("...walk location now at '%s'.", theEntry.getPlayer().getName());
			}

			// If argument is a BOOKMARK modifier
			if (aH.matchesBookmark(thisArg)) {
				out = aH.getBookmarkModifier(thisArg, theEntry.getDenizen());
				if (out != null)
					aH.echoDebug("...walk location now at '%s'.", thisArg);
			}

			else aH.echoError("...unable to match '%s'!", thisArg);
		}

		return out;	

	}

	private Location handleWalk(ScriptEntry theEntry){
		Location out = theEntry.getDenizen().getLocation();
		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			if(thisArg.toUpperCase().contains("FORWARD:")){
				double amt = aH.getIntegerModifier(thisArg);
				Vector victor = theEntry.getDenizen().getLocation().getDirection();
				victor.multiply(amt);
				aH.echoDebug("... offset forward " + amt);
				out.add(victor);
			}
			
			if(thisArg.toUpperCase().contains("NORTH:") || thisArg.toUpperCase().contains("Z:"  )){
				double amt = aH.getIntegerModifier(thisArg);
				out.add(0, 0, amt);
				aH.echoDebug("... offset z " + amt);
			}
			
			if(thisArg.toUpperCase().contains("SOUTH:")){
				double amt = aH.getIntegerModifier(thisArg);
				out.add(0, 0, -amt);
				aH.echoDebug("... offset -z " + amt);
			}
			
			
			if(thisArg.toUpperCase().contains("WEST:") || thisArg.toUpperCase().contains("X:"  )){
				double amt = aH.getIntegerModifier(thisArg);
				out.add(amt, 0, 0);
				aH.echoDebug("... offset x " + amt);
			}
			
			if(thisArg.toUpperCase().contains("EAST:")){
				double amt = aH.getIntegerModifier(thisArg);
				out.add(-amt, 0, 0);
				aH.echoDebug("... offset -x " + amt);
			}
			
			if(thisArg.toUpperCase().contains("UP:") || thisArg.toUpperCase().contains("Y:"  )){
				double amt = aH.getIntegerModifier(thisArg);
				out.add(0, amt, 0);
				aH.echoDebug("... offset y " + amt);
			}
			
			if(thisArg.toUpperCase().contains("DOWN:")){
				double amt = aH.getIntegerModifier(thisArg);
				out.add(0, -amt,0);
				aH.echoDebug("... offset -y " + amt);
			}
			
			
		}

		return out;	

	}


}