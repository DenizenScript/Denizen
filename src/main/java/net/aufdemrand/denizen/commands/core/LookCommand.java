package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;
import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.command.exception.CommandException;


/**
 * Controls Denizen's heads.
 * 
 * @author Jeremy Schroeder
 *
 */

enum Direction { UP, DOWN, LEFT, RIGHT, NORTH, SOUTH, EAST, WEST, BACK, AT, CLOSE, AWAY }

public class LookCommand extends AbstractCommand {

	/* LOOK [DIRECTION|LOCATION BOOKMARK|CLOSE/AWAY]*/

	/* Arguments: [] - Required, () - Optional 
	 * 
	 * [Requires one of the below]
	 * DIRECTION - Valid Directions: UP DOWN LEFT RIGHT NORTH SOUTH EAST WEST BACK AT
	 * LOCATION BOOKMARK - gets Yaw/Pitch from a location bookmark.
	 * CLOSE/AWAY - toggles the NPC's LookClose trait
	 * 
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
		LivingEntity theEntity = null;
		
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

		if (theEntity == null && direction.equals(Direction.AT)) theEntity = (LivingEntity) theCommand.getPlayer();
		if (direction != null || theLocation != null) look(theEntity, theDenizen, direction, duration, theLocation);

		return true;
	}



	private void look(LivingEntity theEntity, DenizenNPC theDenizen, Direction lookDir, Integer duration, Location lookLoc) {

		Location restoreLocation = theDenizen.getEntity().getLocation();
		DenizenNPC restoreDenizen = theDenizen;
		Boolean restoreLookClose = theDenizen.isLookingClose();
		String lookWhere = "NOWHERE";

		if (lookDir != null) lookWhere = lookDir.name();

		if (lookWhere.equals("CLOSE")) {
			theDenizen.lookClose(false);
		}

		else if (lookWhere.equals("AWAY")) {
			theDenizen.lookClose(true);
		}

		else if (lookWhere.equals("LEFT")) {
			theDenizen.lookClose(false);						
			theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() - (float) 80;
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("RIGHT")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() + (float) 80;
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("UP")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().pitch = theDenizen.getHandle().pitch - (float) 60;
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("DOWN")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().pitch = theDenizen.getHandle().pitch + (float) 40;
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;
		}
		
		else if (lookWhere.equals("BACK")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() - 180;
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;			
		}
		
		else if (lookWhere.equals("SOUTH")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 0;
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;			
		}
		
		else if (lookWhere.equals("WEST")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 90;
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;			
		}
		
		else if (lookWhere.equals("NORTH")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 180;
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;			
		}
		
		else if (lookWhere.equals("EAST")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 270;
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;			
		}
	
		else if (lookWhere.equals("AT")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = lookAt(theEntity.getLocation(), theDenizen.getLocation()).getYaw();
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;
		}
		
		else if (lookLoc != null) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().pitch = lookLoc.getPitch();
			theDenizen.getHandle().yaw = lookLoc.getYaw();
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;
		}

		if (duration != null) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new LookCommandRunnable<DenizenNPC, Location, Boolean>(restoreDenizen, restoreLocation, restoreLookClose) {
				@Override
				public void run(DenizenNPC denizen, Location location, Boolean lookClose) { 
					denizen.getHandle().yaw = location.getYaw();
					denizen.getHandle().pitch = location.getPitch();
					denizen.getHandle().as = denizen.getHandle().yaw;				
					denizen.lookClose(lookClose);
				}
			}, duration * 20);
		}


	}
	
	
	/* 
	 * Code below borrowed from bergerkiller
	 * http://forums.bukkit.org/threads/lookat-and-move-functions.26768/
	 * 
	 * Thanks!
	 */
	
	public static Location lookAt(Location loc, Location lookat) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();

        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();

        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }

        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        // Set pitch
        loc.setPitch((float) -Math.atan(dy / dxz));

        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

        return loc;
    }
}

