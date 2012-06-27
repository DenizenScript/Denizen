package net.aufdemrand.denizen.commands.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.QueueType;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnCommand extends Command {

	@Override
	public boolean execute(ScriptCommand theCommand) {

		if (theCommand.arguments().length > 3 || theCommand.arguments().length < 1) {
			theCommand.error("Wrong number of arguments!");
			return false;
		}

		/* SPAWN [ENTITY_TYPE] (AMOUNT) (Location Bookmark) */
		
		try {
			EntityType theEntity = EntityType.valueOf(theCommand.arguments()[0].toUpperCase());	
		} catch (IllegalArgumentException e) {
			theCommand.error("Invalid Entity_Type.");
			return false;
		}
		
		Integer theAmount = null;
		Location theLocation = null;
		
		if (theCommand.arguments().length > 1) {
			if (theCommand.arguments()[1].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
				theAmount = Integer.valueOf(theCommand.arguments()[1]);
			
			else {
				if (theCommand.arguments()[1].split(":").length == 1)
				theLocation = plugin.bookmarks.get(theCommand.getDenizen().getName(), theCommand.arguments()[1], BookmarkType.LOCATION);	
				
				else if (theCommand.arguments()[1].split(":").length == 2)
					theLocation = plugin.bookmarks.get(theCommand.arguments()[1].split(":")[0], theCommand.arguments()[1].split(":")[1], BookmarkType.LOCATION);	
					
				
			
			}
		}
		
		if (theCommand.arguments().length > 2) {
			if (theCommand.arguments()[2].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
				theAmount = Integer.valueOf(theCommand.arguments()[1]);
			else theLocation = plugin.bookmarks.get(theCommand.getDenizen().getName(), theCommand.arguments()[2], BookmarkType.LOCATION);	
		}
		
		
		
		
		
		
		if (!Character.isDigit(theCommand.arguments()[0].charAt(0))) {
			theCommand.error("You must specify a number!");
		}
		
		if (theCommand.getDelayedTime() > theCommand.getInitiatedTime()) {
			/* Second time around, so we've already waited! */
			return true;
		}

		Player thePlayer = theCommand.getPlayer();

		/* WAIT [# OF SECONDS]*/

		theCommand.setDelay(System.currentTimeMillis() + (Long.valueOf(theCommand.arguments()[0]) * 1000));
		theCommand.setInstant();
		List<ScriptCommand> theList = new ArrayList<ScriptCommand>();
		theList.add(theCommand);

		if (theCommand.sendingQueue() == QueueType.TASK) {
			plugin.scriptEngine.injectToQue(thePlayer, theList, QueueType.TASK, 1);
			return true;
		}

		if (theCommand.sendingQueue() == QueueType.TRIGGER) {
			plugin.scriptEngine.injectToQue(thePlayer, theList, QueueType.TRIGGER, 1);
			return true;
		}

		return false;
	}

	
	
	public boolean spawnMob(String mobType, String theAmount, String theLocationBookmark, NPC theDenizen) {
		
		Location theSpawnLoc = null;
		if (theAmount == null) theAmount = "1";
		
		if (theLocationBookmark == null) theSpawnLoc = theDenizen.getBukkitEntity().getLocation();		
		else theSpawnLoc = plugin.bookmarks.get(theDenizen.getName(), theLocationBookmark, BookmarkType.LOCATION);
		
		if (theSpawnLoc != null) {
			for (int cx = 1; cx <= Integer.valueOf(theAmount); cx++) {
				theSpawnLoc.getWorld().spawnCreature(theSpawnLoc, EntityType.valueOf(mobType.toUpperCase()));	
			}
			return true;
		}
		
		return false;
	}

}
