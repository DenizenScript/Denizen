package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.trait.LookClose;

/**
 * Controls Denizens' heads.
 * 
 * @author Jeremy Schroeder
 *
 */

public class LookCommand extends AbstractCommand {

	// TODO: Finish
		
	/* LOOK [[DIRECTION]|[BOOKMARK]:'LOCATION BOOKMARK'|[CLOSE|AWAY]]*/

	/* Arguments: [] - Required, () - Optional 
	 * 
	 * [Requires one of the below]
	 * DIRECTION - Valid Directions: UP DOWN LEFT RIGHT NORTH SOUTH EAST WEST BACK AT
	 * LOCATION BOOKMARK - gets Yaw/Pitch from a location bookmark.
	 * CLOSE/AWAY - toggles the NPC's LookClose trait
	 * 
	 * Modifiers:
	 * (NPCID:#) Changes the Denizen to the Citizens2 NPCID
	 * (DURATION:#) Reverts to the previous head position after # amount of seconds.
	 */

    private enum TargetType { NPC, PLAYER }
    private enum Direction { UP, DOWN, LEFT, RIGHT, NORTH, SOUTH, EAST, WEST, BACK, AT, CLOSE, AWAY }

	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		
	    TargetType targetType = TargetType.NPC;
		Integer duration = null;
		Direction direction = null;
		Location location = null;
		
		for (String arg : scriptEntry.getArguments()) {
			
			// If argument is a duration
			if (aH.matchesDuration(arg)) {
				duration = aH.getIntegerFrom(arg);
				dB.echoDebug("...look duration set to '%s'.", arg);
				continue;
			}
			
        	else if (aH.matchesArg("PLAYER", arg)) {
        		targetType = TargetType.PLAYER;
                dB.echoDebug("... will affect the player!");
        	}
			
			// If argument is a LOCATION modifier
			else if (aH.matchesLocation(arg)) {
				location = aH.getLocationFrom(arg);
				dB.echoDebug("...location set");
            }
			
			// If argument is a Direction
			else {
				for (Direction thisDirection : Direction.values()) {
					if (arg.toUpperCase().equals(thisDirection.name())) {
						direction = Direction.valueOf(arg);
						dB.echoDebug("...set look direction '%s'.", arg);
					}
				}
			}
		}
		
        // If TARGET is NPC/PLAYER and no NPC/PLAYER available, throw exception.
        if (targetType == TargetType.PLAYER && scriptEntry.getPlayer() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        else if (targetType == TargetType.NPC && scriptEntry.getNPC() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);
        scriptEntry.addObject("target", targetType)
        		.addObject("location", location);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		
        TargetType target = (TargetType) scriptEntry.getObject("target");
        Location location = (Location) scriptEntry.getObject("location");
        LivingEntity entity = null;
		
    	if (target.name() == "NPC")
    	{
    		entity = scriptEntry.getNPC().getCitizen().getBukkitEntity();
    		
    		// Turn off the NPC's lookclose
    		scriptEntry.getNPC().getCitizen().getTrait(LookClose.class).lookClose(false);
    	}
    	else
    	{
    		entity = scriptEntry.getPlayer();
    	}

    	if (location != null)
    	{
    		Utilities.faceLocation(entity, location);
    	}
    	
        
	}


	
	
	/*
        // MADE IT THIS FAR

		if (theDenizen == null) {
			aH.echoError("Seems this was sent from a TASK-type script. Must use NPCID:# to specify a Denizen NPC!");
			return false;
		}

		if (theLocation != null) {
			look(theEntity, theDenizen, direction, duration, theLocation);
			return true;
		}

		if (theEntity == null) theEntity = (LivingEntity) theEntry.getPlayer();
		if (direction != null) look(theEntity, theDenizen, direction, duration, theLocation);

		return true;
	}


	private void look(LivingEntity theEntity, dNPC theDenizen, Direction lookDir, Integer duration, Location lookLoc) {

		Location restoreLocation = theDenizen.getEntity().getLocation();
		dNPC restoreDenizen = theDenizen;
		Boolean restoreLookClose = theDenizen.isLookingClose();
		String lookWhere = "NOWHERE";

		if (lookDir != null) lookWhere = lookDir.name();

		if (lookWhere.equals("CLOSE")) {
			theDenizen.lookClose(true);
		}

		else if (lookWhere.equals("AWAY")) {
			theDenizen.lookClose(false);
		}

		else if (lookWhere.equals("LEFT")) {
			theDenizen.lookClose(false);						
			theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() - (float) 80;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("RIGHT")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() + (float) 80;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("UP")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().pitch = theDenizen.getHandle().pitch - (float) 60;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("DOWN")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().pitch = theDenizen.getHandle().pitch + (float) 40;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("BACK")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() - 180;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

		else if (lookWhere.equals("SOUTH")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 0;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

		else if (lookWhere.equals("WEST")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 90;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

		else if (lookWhere.equals("NORTH")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 180;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

		else if (lookWhere.equals("EAST")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 270;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

		else if (lookWhere.equals("AT")) {
			theDenizen.lookClose(false);
			faceEntity(theDenizen.getEntity(), theEntity);
		}

		else if (lookLoc != null) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().pitch = lookLoc.getPitch();
			theDenizen.getHandle().yaw = lookLoc.getYaw();
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;
		}


		// If duration is set...

		if (duration != null) {

			if (taskMap.containsKey(theDenizen.getCitizensEntity().getId())) {
				try {
					plugin.getServer().getScheduler().cancelTask(taskMap.get(theDenizen.getId()));
				} catch (Exception e) { }
			}

			aH.echoDebug("Setting delayed task: RESET LOOK");

			taskMap.put(theDenizen.getCitizensEntity().getId(), plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new FourItemRunnable<dNPC, Location, Boolean, Float>(restoreDenizen, restoreLocation, restoreLookClose, theDenizen.getLocation().getYaw()) {
				@Override
				public void run(dNPC denizen, Location location, Boolean lookClose, Float checkYaw) {
					aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: RESET LOOK.");
					denizen.lookClose(lookClose);

					//				if (denizen.getLocation().getYaw() == checkYaw) {
					denizen.getHandle().yaw = location.getYaw();
					denizen.getHandle().pitch = location.getPitch();
					denizen.getHandle().az = denizen.getHandle().yaw;				
					//				}
				}
			}, duration * 20));
		}


	}

*/
}

