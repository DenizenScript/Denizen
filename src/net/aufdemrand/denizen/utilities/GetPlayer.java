package net.aufdemrand.denizen.utilities;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GetPlayer {



	/*
	 * getPlayersInRange
	 * 
	 * Returns a List<Player> of players within a range to the specified Denizen.
	 * 
	 */

	public List<Player> getInRange (LivingEntity theEntity, int theRange) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		List<Player> PlayersWithinRange = new ArrayList<Player>();

		Player[] DenizenPlayers = plugin.getServer().getOnlinePlayers();

		for (Player aPlayer : DenizenPlayers) {
			if (aPlayer.isOnline() 
					&& aPlayer.getWorld().equals(theEntity.getWorld()) 
					&& aPlayer.getLocation().distance(theEntity.getLocation()) < theRange)
				PlayersWithinRange.add(aPlayer);
		}

		return PlayersWithinRange;
	}

	public List<Player> getInRange (LivingEntity theEntity, int theRange, Player excludePlayer) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		List<Player> PlayersWithinRange = new ArrayList<Player>();

		Player[] DenizenPlayers = plugin.getServer().getOnlinePlayers();

		for (Player aPlayer : DenizenPlayers) {
			if (aPlayer.isOnline() 
					&& aPlayer.getWorld().equals(theEntity.getWorld()) 
					&& aPlayer.getLocation().distance(theEntity.getLocation()) < theRange)
				PlayersWithinRange.add(aPlayer);
		}

		PlayersWithinRange.remove(excludePlayer);

		return PlayersWithinRange;
	}



	/* talkToDenizen
	 *
	 * Sends the message from Player to Denizen with the formatting
	 * as specified in the config.yml talk_to_npc_string.
	 *
	 * <NPC> and <TEXT> are replaced with corresponding information.
	 */

	public void talkToDenizen(NPC theDenizen, Player thePlayer, String theMessage) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		

		thePlayer.sendMessage(plugin.getConfig().getString("player_chat_to_npc", "You say to <NPC>, <TEXT>")
				.replace("<NPC>", theDenizen.getName())
				.replace("<TEXT>", theMessage)
				.replace("<PLAYER>", thePlayer.getName()));

	}



	/**
	 * Checks the players level against information 
	 *
	 * @param  thePlayer  The bukkit Player object of the player being checked.
	 * @param  theLevel  String value of the level being checked against.
	 * @param  highLevel  String value of the high level being checked against, if specifying a low and high number. (OPTIONAL: May set to NULL if only checking one level.)
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkLevel(Player thePlayer, String theLevel, String highLevel, boolean negativeRequirement) {

		boolean outcome = false;

		try {

			/*
			 * (-)LEVEL [#]
			 */
			
			if (highLevel == null) {
				if (thePlayer.getLevel() >= Integer.valueOf(theLevel)) outcome = true;
			}

			/*
			 * (-)LEVEL [#] [#]
			 */

			if (highLevel != null) {
				if (thePlayer.getLevel() >= Integer.valueOf(theLevel)
						&& thePlayer.getLevel() <= Integer.valueOf(highLevel)) outcome = true;
			}

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}



	/**
	 * Checks the players saturation information 
	 *
	 * @param  thePlayer  The bukkit Player object of the player being checked.
	 * @param  saturationType  String value of the saturation type being checked against. Valid types: FULL, HUNGRY, STARVING
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkSaturation(Player thePlayer, String saturationType, boolean negativeRequirement) {

		boolean outcome = false;

		try {

			/*
			 * (-)HUNGER [FULL|STARVING|HUNGRY]
			 */
			
			if (saturationType.equalsIgnoreCase("STARVING")
					&& thePlayer.getFoodLevel() <= 2) outcome = true;

			if (saturationType.equalsIgnoreCase("HUNGRY")
					&& thePlayer.getFoodLevel() <= 18) outcome = true;

			if (saturationType.equalsIgnoreCase("FULL")
					&& thePlayer.getFoodLevel() >= 18) outcome = true;

			else throw new Error("HUNGER requirement error. Check Syntax.");

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}






}