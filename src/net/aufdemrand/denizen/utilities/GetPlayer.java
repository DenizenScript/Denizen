package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

		List<Player> PlayersWithinRange = getInRange(theEntity, theRange);
		PlayersWithinRange.remove(excludePlayer);

		return PlayersWithinRange;
	}



	/*
	 * getInventoryMap
	 * 
	 * Returns a Map<Material, Integer> of the players inventory. Unlike bukkit's getInventory.getContents(),
	 * this returns a total count of material and quantity, so quantities are not limited to
	 * the size of maxStackSize. For example, if a player has 2 stacks of wood, 35 in each, this will return
	 * the total number of wood as 70.
	 * 
	 */

	public Map<Material, Integer> getInventoryMap(Player thePlayer) {

		Map<Material, Integer> playerInv = new HashMap<Material, Integer>();
		ItemStack[] getContentsArray = thePlayer.getInventory().getContents();
		List<ItemStack> getContents = Arrays.asList(getContentsArray);

		for (int x=0; x < getContents.size(); x++) {
			if (getContents.get(x) != null) {

				if (playerInv.containsKey(getContents.get(x).getType())) {
					int t = playerInv.get(getContents.get(x).getType());
					t = t + getContents.get(x).getAmount(); playerInv.put(getContents.get(x).getType(), t);
				}

				else playerInv.put(getContents.get(x).getType(), getContents.get(x).getAmount());
			}
		}

		return playerInv;
	}



	public Map<String, Integer> getInventoryIdMap(Player thePlayer) {

		Map<String, Integer> playerInv = new HashMap<String, Integer>();
		ItemStack[] getContentsArray = thePlayer.getInventory().getContents();
		List<ItemStack> getContents = Arrays.asList(getContentsArray);
		String workingItem = null;
		
		for (int x=0; x < getContents.size(); x++) {
			if (getContents.get(x) != null) {

				if (playerInv.containsKey(getContents.get(x).getType())) {
					int t = playerInv.get(getContents.get(x).getType());
					t = t + getContents.get(x).getAmount(); playerInv.put(getContents.get(x).getTypeId(), t);
				}

				else playerInv.put(getContents.get(x).getTypeId(), getContents.get(x).getAmount());
			}
		}

		return playerInv;
	}
	
	public int getData(String theArgs) {
		int theData = Integer.valueOf(theArgs.split(":", 2)[1]);
		
		return theData;
	}
	
	public int getItem(String theArgs) {
		int theItem = Integer.valueOf(theArgs.split(":", 2)[0]);
		
		return theItem;
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

		return;
	}



	/**
	 * Checks the players level against information 
	 *
	 * @param  thePlayer The bukkit Player object of the player being checked.
	 * @param  theLevel  String value of the level being checked against.
	 * @param  highLevel String value of the high level being checked against, if specifying a low and high number. (OPTIONAL: May set to NULL if only checking one level.)
	 * @param  negativeRequirement Set to true if this is a negative (-) requirement.
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



	public boolean checkName(Player thePlayer, List<String> theNames, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)NAME [List of Names]
		 */

		try {

			if (theNames.contains(thePlayer.getName())) outcome = true;

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}



	public boolean checkFunds(Player thePlayer, String theFunds, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)MONEY [#]
		 */

		try {

			if (Denizen.denizenEcon.has(thePlayer.getName(), Double.parseDouble(theFunds))) outcome = true;

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}



	public boolean checkInventory(Player thePlayer, String theItem, String theAmount, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)ITEM [ITEM_NAME|#:#] [#]
		 */



		try {

			if (Character.isDigit(theItem.charAt(0))) {
				if (getInventoryIdMap(thePlayer).containsKey(Integer.valueOf(theItem))) {
					if (getInventoryIdMap(thePlayer).get(Integer.valueOf(theItem)) >= Integer.valueOf(theAmount)) 
						outcome = true;	
				}
			}
			else {
				if (getInventoryMap(thePlayer).containsKey(Material.valueOf(theItem))) {
					if (getInventoryMap(thePlayer).get(Material.valueOf(theItem)) >= Integer.valueOf(theAmount)) 
						outcome = true;
				}
			}


		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}




	public boolean checkHand(Player thePlayer, String theItem, String theAmount, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)HOLDING [ITEM_NAME|#:#] [#]
		 */

		if (theAmount == null) theAmount = "1";

		try {

			if (Character.isDigit(theItem.charAt(0))) {
				if (thePlayer.getItemInHand().getTypeId() == Integer.valueOf(theItem)
						&& thePlayer.getItemInHand().getAmount() >= Integer.valueOf(theAmount)) 
					outcome = true;
			}
			else {
				if (thePlayer.getItemInHand().getTypeId() == Integer.valueOf(theItem)
						&& thePlayer.getItemInHand().getAmount() >= Integer.valueOf(theAmount)) 
					outcome = true;
			}

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}








}