package net.aufdemrand.denizen.commands.core;

import org.bukkit.Location;

import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;

/**
 * Makes Denizens move in different ways. WALK/RUN/WALKTO/RETURN/LOOK/NOD/JUMP/CROUCH/SNEAK
 * 
 * @author Jeremy Schroeder
 *
 */

public class MoveCommands extends Command {

	/* ENGAGE (# of Seconds) */

	/* Arguments: [] - Required, () - Optional 
	 * (# of Seconds) Will automatically DISENGAGE after specified amount of seconds.
	 *   If not set, the Denizen will remain ENGAGED until a DISENGAGE command is used.
	 *   
	 * Modifiers:
	 * (NPCID:#) Changes the Denizen to ENGAGE or DISENGAGE to the Citizens2 NPCID
	 */

	/* DISENGAGE */

	/* Modifiers:
	 * (NPCID:#) Changes the Denizen to ENGAGE or DISENGAGE to the Citizens2 NPCID
	 */

	@Override
	public boolean execute(ScriptCommand theCommand) {

		/* Initialize variables */ 

		Integer timedEngage = null;
		NPC theDenizen = theCommand.getDenizen();

		/* Get arguments */
		for (String thisArgument : theCommand.arguments()) {
			if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
				timedEngage = Integer.valueOf(thisArgument);

			if (thisArgument.toUpperCase().contains("NPCID:"))
				try {
					if (CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(thisArgument.split(":")[1])) != null)
						theDenizen = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(thisArgument.split(":")[1]));	
				} catch (Throwable e) {
					theCommand.error("NPCID specified could not be matched to a Denizen.");
					return false;
				}
		}	

		/* If a DISENGAGE, take the Denizen out of the List. */
		if (theCommand.getCommand().equalsIgnoreCase("DISENGAGE")) {
			plugin.scriptEngine.setEngaged(theCommand.getDenizen(), false);
			return true;
		}

		/* ENGAGE the Denizen and set timer for DISENGAGE (if arguement is specified) */
		if (timedEngage != null) 
			plugin.scriptEngine.setEngaged(theDenizen, timedEngage);
		else 			
			plugin.scriptEngine.setEngaged(theCommand.getDenizen(), true);

		return true;
	}


	private void look(NPC theDenizen, String lookWhere, Integer duration) {

		final Location restoreLocation = theDenizen.getBukkitEntity().getLocation();
		final NPC restoreDenizen = theDenizen;

		if (lookWhere.equalsIgnoreCase("CLOSE")) {
			if (!theDenizen.getTrait(LookClose.class).toggle())
				theDenizen.getTrait(LookClose.class).toggle();
		}

		else if (lookWhere.equalsIgnoreCase("AWAY")) {
			if (theDenizen.getTrait(LookClose.class).toggle())
				theDenizen.getTrait(LookClose.class).toggle();
		}

		else if (lookWhere.equalsIgnoreCase("LEFT")) {
			theDenizen.getBukkitEntity().getLocation()
			.setYaw(theDenizen.getBukkitEntity().getLocation().getYaw() - 90);
		}

		else if (lookWhere.equalsIgnoreCase("RIGHT")) {
			theDenizen.getBukkitEntity().getLocation()
			.setYaw(theDenizen.getBukkitEntity().getLocation().getYaw() + 90);
		}

		else if (plugin.bookmarks.exists(theDenizen, lookWhere)) {
			Location lookLoc = plugin.bookmarks.get(theDenizen.getName(), lookWhere, BookmarkType.LOCATION);
			theDenizen.getBukkitEntity().getLocation().setPitch(lookLoc.getPitch());
			theDenizen.getBukkitEntity().getLocation().setYaw(lookLoc.getYaw());
		}

		if (duration != null) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() { 
					restoreDenizen.getBukkitEntity().getLocation().setYaw(restoreLocation.getYaw());
				}
			}, duration * 20);
		}


	}
}