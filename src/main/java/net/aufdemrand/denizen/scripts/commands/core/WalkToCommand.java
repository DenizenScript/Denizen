package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Your command! 
 * This class is a template for a Command in Denizen.
 * 
 * @author Jeremy Schroeder (aufdemrand)
 */
public class WalkToCommand extends AbstractCommand {
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize required fields
		Location location = null;
		float speed = -1f;

		for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesLocation(arg))
                location = aH.getLocationFrom(arg);

            else if (aH.matchesValueArg("SPEED", arg, aH.ArgumentType.Double))
                speed = (float) aH.getDoubleFrom(arg);

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		scriptEntry.addObject("location", location)
		    .addObject("speed", speed);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Location location = (Location) scriptEntry.getObject("location");
        Float speed = (Float) scriptEntry.getObject("speed");

        scriptEntry.getNPC().getNavigator().setTarget(location);
        
        if (speed > 0)
            scriptEntry.getNPC().getNavigator().getLocalParameters().speedModifier(speed);

	}



	/*
	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		 
		Location walkLocation = null;
		boolean returning = false;
		Float Speed = null;

		if (theEntry.getCommand().equalsIgnoreCase("return")) {
			if (returns.containsKey(theEntry.getDenizen())){
				walkLocation = returns.get(theEntry.getDenizen());
				returning = true;
			}
			lse
			{
				aH.echoDebug("Return location not found for " + theEntry.getDenizen().getName());
				return false;
			}
		}

		for (String thisArg : theEntry.arguments()) {
			
			// Fill replaceables
			if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);
			
			if(thisArg.toUpperCase().contains("SPEED:")){

				try {
					Speed = Float.valueOf( aH.getStringModifier(thisArg));		
					aH.echoDebug("... speed set to " + Speed);
				} catch (Exception e) {
					aH.echoDebug("... Invalid Speed!");
				}
			}
		}

		if(theEntry.getCommand().equalsIgnoreCase("walkto"))  {
			walkLocation = handleWalkTo(theEntry);
		}
		else if(theEntry.getCommand().equalsIgnoreCase("walk"))  {
			walkLocation = handleWalk(theEntry);
		}

		
		if (walkLocation != null) {

			double dist = theEntry.getDenizen().getLocation().distance(walkLocation);
			theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(true);
			theEntry.getDenizen().getNavigator().cancelNavigation();
			theEntry.getDenizen().getCitizensEntity().getNavigator().setTarget(walkLocation);
			if (theEntry.getDenizen().getNavigator().getDefaultParameters().range() < dist ) theEntry.getDenizen().getNavigator().getLocalParameters().range((float) (dist + 3)); 
			if(Speed!=null) theEntry.getDenizen().getCitizensEntity().getNavigator().getDefaultParameters().speedModifier(Speed);
			if (returning) theEntry.getDenizen().getCitizensEntity().getTrait(Waypoints.class).getCurrentProvider().setPaused(false);
			else returns.put(theEntry.getDenizen(), walkLocation);
			return true;

		}
		else	aH.echoDebug("...No location!");
		
		
		return false;
	}

	private Location handleWalkTo(ScriptEntry theEntry){
		Location out = null;
		
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
		
		for (String thisArg : theEntry.arguments()) {

			if(thisArg.toUpperCase().contains("FORWARD:")){
				double amt = Double.valueOf( aH.getStringModifier(thisArg));
				Vector victor = theEntry.getDenizen().getLocation().getDirection();
				victor.multiply(amt);
				aH.echoDebug("... offset forward " + amt);
				out.add(victor);
			}
			
			if(thisArg.toUpperCase().contains("NORTH:") || thisArg.toUpperCase().contains("Z:"  )){
				double amt = Double.valueOf( aH.getStringModifier(thisArg));
				out.add(0, 0, amt);
				aH.echoDebug("... offset z " + amt);
			}
			
			if(thisArg.toUpperCase().contains("SOUTH:")){
				double amt = Double.valueOf( aH.getStringModifier(thisArg));
				out.add(0, 0, -amt);
				aH.echoDebug("... offset -z " + amt);
			}
			
			
			if(thisArg.toUpperCase().contains("WEST:") || thisArg.toUpperCase().contains("X:"  )){
				double amt = Double.valueOf( aH.getStringModifier(thisArg));
				out.add(amt, 0, 0);
				aH.echoDebug("... offset x " + amt);
			}
			
			if(thisArg.toUpperCase().contains("EAST:")){
				double amt = Double.valueOf( aH.getStringModifier(thisArg));
				out.add(-amt, 0, 0);
				aH.echoDebug("... offset -x " + amt);
			}
			
			if(thisArg.toUpperCase().contains("UP:") || thisArg.toUpperCase().contains("Y:"  )){
				double amt = Double.valueOf( aH.getStringModifier(thisArg));
				out.add(0, amt, 0);
				aH.echoDebug("... offset y " + amt);
			}
			
			if(thisArg.toUpperCase().contains("DOWN:")){
				double amt = Double.valueOf( aH.getStringModifier(thisArg));
				out.add(0, -amt,0);
				aH.echoDebug("... offset -y " + amt);
			}
			
			
		}

		return out;	
	}
 */

	
}