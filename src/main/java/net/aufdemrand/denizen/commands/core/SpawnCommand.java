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

		/* SPAWN [ENTITY_TYPE] (QUANTITY) (Location Bookmark|Denizen Name:Location Bookmark) */

		EntityType theEntity = null;
		Integer theAmount = null;
		Location theLocation = null;

		for (String thisArgument : theCommand.arguments()) {

			// If a valid name of an Entity, set theEntity.
			if (plugin.utilities.isEntity(thisArgument)) {
				theEntity = EntityType.valueOf(thisArgument.toUpperCase());	
			}
			
			// If argument is a #, set theAmount.
			else if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
				theAmount = Integer.valueOf(thisArgument);

			// If argument is a valid bookmark, set theLocation.
			else if (plugin.bookmarks.exists(theCommand.getDenizen(), thisArgument))
				theLocation = plugin.bookmarks.get(theCommand.getDenizen(), thisArgument, BookmarkType.LOCATION);	
		}
		
		/* If theAmount or theLocation is STILL empty, let's try to fill it automatically */		

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
