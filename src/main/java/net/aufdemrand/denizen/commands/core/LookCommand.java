package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;
import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.*;
import net.minecraft.server.EntityLiving;


/**
 * Controls Denizen's heads.
 * 
 * @author Jeremy Schroeder
 *
 */

enum Direction { UP, DOWN, LEFT, RIGHT, NORTH, SOUTH, EAST, WEST, BACK, AT, CLOSE, AWAY }

public class LookCommand extends AbstractCommand {

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


	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {


		/* Initialize variables */ 

		Integer duration = null;
		Direction direction = null;
		Location theLocation = null;
		LivingEntity theEntity = null;

		DenizenNPC theDenizen = theEntry.getDenizen();

		/* Get arguments */
		for (String thisArg : theEntry.arguments()) {

			// If argument is a duration
			if (aH.matchesDuration(thisArg)) {
				duration = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...look duration set to '%s'.", thisArg);
			}

			// If argument is a NPCID: modifier
			else if (aH.matchesNPCID(thisArg)) {
				theDenizen = aH.getNPCIDModifier(thisArg);
				if (theDenizen != null)
					aH.echoDebug("...affecting '%s'.", thisArg);
			}

			// If argument is a BOOKMARK modifier
			else if (aH.matchesBookmark(thisArg)) {
				theLocation = aH.getBookmarkModifier(thisArg, theEntry.getDenizen());
				if (theLocation != null)
					aH.echoDebug("...drop location now at bookmark '%s'.", thisArg);
			}


			else {
				/* If argument is a Direction */
				for (Direction thisDirection : Direction.values()) {
					if (thisArg.toUpperCase().equals(thisDirection.name())) {
						direction = Direction.valueOf(thisArg);
						aH.echoDebug("...set look direction '%s'.", thisArg);
					}
				}			
			}
			
			// End Direction
		}	

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


	private void look(LivingEntity theEntity, DenizenNPC theDenizen, Direction lookDir, Integer duration, Location lookLoc) {

		Location restoreLocation = theDenizen.getEntity().getLocation();
		DenizenNPC restoreDenizen = theDenizen;
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
			faceEntity(theDenizen.getEntity(), theEntity);
		}

		else if (lookLoc != null) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().pitch = lookLoc.getPitch();
			theDenizen.getHandle().yaw = lookLoc.getYaw();
			theDenizen.getHandle().as = theDenizen.getHandle().yaw;
		}


		// If duration is set...

		if (duration != null) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new LookCommandRunnable<DenizenNPC, Location, Boolean, Float>(restoreDenizen, restoreLocation, restoreLookClose, theDenizen.getLocation().getYaw()) {
				@Override
				public void run(DenizenNPC denizen, Location location, Boolean lookClose, Float checkYaw) { 

					denizen.lookClose(lookClose);

					//				if (denizen.getLocation().getYaw() == checkYaw) {
					denizen.getHandle().yaw = location.getYaw();
					denizen.getHandle().pitch = location.getPitch();
					denizen.getHandle().as = denizen.getHandle().yaw;				
					//				}
				}
			}, duration * 20);
		}
		
		
	}



	// Thanks fullwall

	private void faceEntity(Entity from, Entity at) {
		if (from.getWorld() != at.getWorld())
			return;
		Location loc = from.getLocation();

		double xDiff = at.getLocation().getX() - loc.getX();
		double yDiff = at.getLocation().getY() - loc.getY();
		double zDiff = at.getLocation().getZ() - loc.getZ();

		double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
		double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

		double yaw = (Math.acos(xDiff / distanceXZ) * 180 / Math.PI);
		double pitch = (Math.acos(yDiff / distanceY) * 180 / Math.PI) - 90;
		if (zDiff < 0.0) {
			yaw = yaw + (Math.abs(180 - yaw) * 2);
		}

		EntityLiving handle = ((CraftLivingEntity) from).getHandle();
		handle.yaw = (float) yaw - 90;
		handle.pitch = (float) pitch;
		handle.as = handle.yaw;
	}

}

