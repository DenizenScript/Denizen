package net.aufdemrand.denizen.utilities;

import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class GetWorld {

	private Denizen plugin;

	public GetWorld(Denizen denizen) {
		plugin = denizen;
	}



	/**
	 * Checks the time in the specified bukkit World. 
	 *
	 * @param  theWorld The bukkit World object of the world being checked.
	 * @param  theTime String value of the time being checked against. Valid types: [NIGHT, DAY, DUSK, DAWN]. Can also be a # from 0-23999 indicating Minecraft time, but must be used with highTime.
	 * @param  highTime Can be 1-24000, the high end of the range of times to check against in conjunction with theTime.
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkTime(World theWorld, String theTime, String highTime, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)TIME [DAWN|DAY|DUSK|NIGHT]  or  (-)TIME [#] [#]
		 */

		try {
			if (!Character.isDigit(theTime.charAt(0))) {
				if (theTime.equalsIgnoreCase("DAWN")
						&& theWorld.getTime() > 23000) outcome = true;

				else if (theTime.equalsIgnoreCase("DAY")
						&& theWorld.getTime() > 0
						&& theWorld.getTime() < 13500) outcome = true;

				else if (theTime.equalsIgnoreCase("DUSK")
						&& theWorld.getTime() > 12500
						&& theWorld.getTime() < 13500) outcome = true;

				else if (theTime.equalsIgnoreCase("NIGHT")
						&& theWorld.getTime() > 13500) outcome = true;
			}

			else if (Character.isDigit(theTime.charAt(0))) 
				if (theWorld.getTime() > Long.valueOf(theTime)
						&& theWorld.getTime() < Long.valueOf(highTime)) outcome = true;

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured with the TIME requirement.");
			Bukkit.getLogger().info("Error follows: " + error);
		}


		if (negativeRequirement != outcome) return true;

		return false;
	}

	
	


	/**
	 * Checks the weather in the specified bukkit World. 
	 *
	 * @param  theWorld The bukkit World object of the world being checked.
	 * @param  weatherType String value of the weather being checked. Valid types: [PRECIPITATION, SUNNY]
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkWeather(World theWorld, String weatherType, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)SUNNY  or  (-)PRECIPITATION
		 */

		try {

			if (weatherType.equalsIgnoreCase("PRECIPITATION")
					&& theWorld.hasStorm()) outcome = true;

			else if (weatherType.equalsIgnoreCase("SUNNY")
					&& !theWorld.hasStorm()) outcome = true;

			else throw new Error("checkWeather requirement error. Check Syntax.");

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured with a WEATHER requirement.");
			Bukkit.getLogger().info("Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}


	
	

	public boolean checkWorld(LivingEntity theEntity, List<String> theWorlds, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)WORLD [List of Worlds]
		 */

		try {

			if (theWorlds.contains(theEntity.getWorld().getName().toUpperCase())) outcome = true;

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured with the WORLD requirement.");
			Bukkit.getLogger().info("Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

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
