package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class SpawnCommand extends Command {

	/* SPAWN command 
	 * 
	 * Spawns a mobile.
	 * 
	 * Arguments: [] - Required, () - Optional 
	 * [ENTITY_TYPE] 
	 * (QUANTITY) Will default to '1' if not specified
	 * (LOCATION BOOKMARK) Will default to the player location if not specified
	 * 
	 * Modifiers:
	 * ('SPREAD:#') Increases the 'spread' of the area that the monster can spawn. 
	 * ('EFFECT:POTION_EFFECT MODIFIER') Applies a potion effect on the monster when spawning.
	 * ('FLAG:CHARGED|SADDLE|BABY|PROFESSION [PROFESSION_TYPE]|SHEARED|ANGRY')
	 *   Applies a flag to the Mob. Note: Only works for mobs that can accept the flag.
	 *   ie. Only Creepers can be CHARGED, only Pigs can have a SADDLE, etc.
	 * 
	 * Example usages:
	 * SPAWN ZOMBIE
	 * SPAWN COW 3 Cage
	 * SPAWN VILLAGER 'Joseph the Great:Gate'
	 * SPAWN PIG_ZOMBIE 10 'SPREAD:5' 'FLAG:SADDLE'
	 * SPAWN 
	 * 
	 * */
	
	@Override
	public boolean execute(ScriptCommand theCommand) {

		if (theCommand.arguments().length < 1) {
			theCommand.error("Not enough arguments!");
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
		
			// Warn the console that argument has been ignored.
			else {
				plugin.getLogger().log(Level.WARNING, "Unknown argument for " + theCommand.getCommand() + " command in script '" + theCommand.getScript() + "': " + thisArgument);
				plugin.getLogger().log(Level.WARNING, "This argument has been ignored.");
			}
			
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
