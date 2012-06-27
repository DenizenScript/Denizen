package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class SpawnCommand extends Command {

	@Override
	public boolean execute(ScriptCommand theCommand) {

		if (theCommand.arguments().length > 3 || theCommand.arguments().length < 1) {
			theCommand.error("Wrong number of arguments!");
			return false;
		}

		/* SPAWN [ENTITY_TYPE] (AMOUNT) (Location Bookmark|Denizen Name:Location Bookmark) */

		EntityType theEntity = null;
		Integer theAmount = null;
		Location theLocation = null;

		try {
			theEntity = EntityType.valueOf(theCommand.arguments()[0].toUpperCase());	
		} catch (IllegalArgumentException e) {
			theCommand.error("Invalid Entity_Type.");
			return false;
		}

		/* Attempt to identify the first argument */
		if (theCommand.arguments().length > 1) {
			if (theCommand.arguments()[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
				theAmount = Integer.valueOf(theCommand.arguments()[1]);
			else {
				if (theCommand.arguments()[1].split(":").length == 1) {
					if (theCommand.getDenizen() == null) {
						theCommand.error("No Denizen referenced for the bookmarked location.");
						return false;						
					}
					theLocation = plugin.bookmarks.get(theCommand.getDenizen().getName(), theCommand.arguments()[1], BookmarkType.LOCATION);
				}	
				else if (theCommand.arguments()[1].split(":").length == 2)
					theLocation = plugin.bookmarks.get(theCommand.arguments()[1].split(":")[0], theCommand.arguments()[1].split(":")[1], BookmarkType.LOCATION);	
			}
		}

		/* Attempt to identify the second argument */
		if (theCommand.arguments().length > 2) {
			if (theCommand.arguments()[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
				theAmount = Integer.valueOf(theCommand.arguments()[2]);
			else {
				if (theCommand.arguments()[2].split(":").length == 1) {
					if (theCommand.getDenizen() == null) {
						theCommand.error("No Denizen referenced for the bookmarked location.");
						return false;						
					}
					theLocation = plugin.bookmarks.get(theCommand.getDenizen().getName(), theCommand.arguments()[2], BookmarkType.LOCATION);
				}	
				else if (theCommand.arguments()[2].split(":").length == 2)
					theLocation = plugin.bookmarks.get(theCommand.arguments()[1].split(":")[0], theCommand.arguments()[2].split(":")[1], BookmarkType.LOCATION);	
			}
		}

		/* If theAmount or theLocation is STILL empty, let's try to fill it */		
		if (theAmount == null) theAmount = 1;
		if (theLocation == null && theCommand.getDenizen() != null) 
			theLocation = theCommand.getDenizen().getBukkitEntity().getLocation();
		if (theLocation == null) theLocation = theCommand.getPlayer().getLocation();

		/* Now the creature spawning! */
		if (theLocation != null && theAmount != null && theEntity != null) {
			for (int x = 0; x < theAmount; x++)
				theLocation.getWorld().spawnCreature(theLocation, theEntity);
			return true;
		}

		theCommand.error("Unknown error. Check syntax.");
		return false;
	}



}
