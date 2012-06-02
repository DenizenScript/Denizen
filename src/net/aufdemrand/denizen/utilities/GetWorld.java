package net.aufdemrand.denizen.utilities;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GetWorld {



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

			else if (theWorld.getTime() > Long.valueOf(theTime)
					&& theWorld.getTime() < Long.valueOf(highTime)) outcome = true;

			else throw new Error("TIME requirement error. Check Syntax.");

		} catch(Throwable error) {

			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);

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
			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}




}
