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
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffectType;

public class GetPlayer {

	/**
	 * Gets players in range of a bukkit Entity. 
	 *
	 * @param  theEntity  the bukkit Entity to check for players around.
	 * @param  theRange  the Range, in blocks, to check around theEntity.
	 * @return  returns a list of Players around theEntity.
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

	/**
	 * Gets players in range of a bukkit Entity, excluding a specified Player. 
	 *
	 * @param  theEntity  the bukkit Entity to check for players around.
	 * @param  theRange  the Range, in blocks, to check around theEntity.
	 * @param  excludePlayer  the bukkit Player to exclude from the returned list.
	 * @return  returns a list of Players around theEntity, excluding the excludePlayer.
	 */

	public List<Player> getInRange (LivingEntity theEntity, int theRange, Player excludePlayer) {

		List<Player> PlayersWithinRange = getInRange(theEntity, theRange);
		PlayersWithinRange.remove(excludePlayer);

		return PlayersWithinRange;
	}






	/**
	 * Gets a Map of a player's inventory with a bukkit Material and Integer amount for each item. Unlike bukkit's build in getInventory, this will add up the total number of each Material. 
	 *
	 * @param  thePlayer  the Player whose inventory is being checked.
	 * @return  returns a Map<Material, Integer>.
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

	/**
	 * Alternate usage that gets a Map of a player's inventory with a String representation of itemID:data and Integer amount for each item. Unlike bukkit's build in getInventory, this will add up the total number of each itemID. 
	 *
	 * @param  thePlayer  the Player whose inventory is being checked.
	 * @return  returns a Map<String, Integer>.
	 */

	public Map<String, Integer> getInventoryIdMap(Player thePlayer) {

		Map<String, Integer> playerInv = new HashMap<String, Integer>();
		ItemStack[] getContentsArray = thePlayer.getInventory().getContents();
		List<ItemStack> getContents = Arrays.asList(getContentsArray);

		for (int x=0; x < getContents.size(); x++) {
			if (getContents.get(x) != null) {
				MaterialData specificItem = getContents.get(x).getData();
				String friendlyItem = specificItem.getItemTypeId() + ":" + specificItem.getData();

				if (playerInv.containsKey(friendlyItem)) {
					int t = playerInv.get(friendlyItem);
					t = t + getContents.get(x).getAmount(); playerInv.put(friendlyItem, t);
				}
				else playerInv.put(friendlyItem, getContents.get(x).getAmount());
			}
		}

		return playerInv;
	}






	/**
	 * Talks to a NPC. Also has replaceable data, end-user, when using <NPC> <TEXT> <PLAYER> <FULLPLAYERNAME> <WORLD> or <HEALTH>.
	 *
	 * @param  theDenizen  the Citizens2 NPC object to talk to.
	 * @param  thePlayer  the Bukkit Player object that is doing the talking.
	 */

	public void talkToDenizen(NPC theDenizen, Player thePlayer, String theMessage) {
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		

		thePlayer.sendMessage(plugin.getConfig().getString("player_chat_to_npc", "You say to <NPC>, <TEXT>")
				.replace("<NPC>", theDenizen.getName())
				.replace("<TEXT>", theMessage)
				.replace("<PLAYER>", thePlayer.getName())
				.replace("<FULLPLAYERNAME>", thePlayer.getDisplayName())
				.replace("<WORLD>", thePlayer.getWorld().getName())
				.replace("<HEALTH>", String.valueOf(thePlayer.getHealth())));
		return;
	}






	/**
	 * Checks the player's level. 
	 *
	 * @param  thePlayer The bukkit Player object of the player being checked.
	 * @param  theLevel  the String value of the level being checked against.
	 * @param  highLevel String value of the high level being checked against, if specifying a low and high number. Can also be set to null.
	 * @param  negativeRequirement set to true if this is a negative (-) requirement.
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
	 * Checks the players saturation levels.
	 *
	 * @param  thePlayer  The Player being checked.
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






	/**
	 * Checks the name of thePlayer against a supplied List<String> of player names. 
	 *
	 * @param  thePlayer  the Player to compare to.
	 * @param  theNames  the List<String> of names to check against.
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.	 */


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





	/**
	 * Checks the funds of the player against a given value with Vault. 
	 *
	 * @param  thePlayer  the Player to check funds of.
	 * @param  theFunds  the String representation of the Integer of funds to check for. 
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

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





	/**
	 * Checks the Inventory of the player against a given value. 
	 *
	 * @param  thePlayer  the Player whose inventory shall be checked.
	 * @param  theItem  the String representation of the item to check for. Can use a bukkit Material, such as IRON_ORE, or can use typeID format, such as 44. 
	 * @param  theData  the String representation of the Data value of the item being checked. Only used when dealing with a typeID format of theItem, otherwise can be null. 
	 * @param  theAmount  the String representation of the Integer amount of the item to check for. If null, 1 is assumed.
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkInventory(Player thePlayer, String theItem, String theData, String theAmount, boolean negativeRequirement) {

		boolean outcome = false;

		if (theAmount == null) theAmount = "1";

		/*
		 * (-)ITEM [ITEM_NAME|#:#] [#]
		 */

		try {

			if (Character.isDigit(theItem.charAt(0))) {

				if (theData == null) theData = "0";
				String friendlyItem = theItem + ":" + theData;
				if (getInventoryIdMap(thePlayer).containsKey(friendlyItem)) {
					if (getInventoryIdMap(thePlayer).get(friendlyItem) >= Integer.valueOf(theAmount)) 
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





	/**
	 * Checks the item in hand of the player against a given value. 
	 *
	 * @param  thePlayer  the Player whose inventory shall be checked.
	 * @param  theItem  the String representation of the item to check for. Can use a bukkit Material, such as IRON_ORE, or can use typeID format, such as 44. 
	 * @param  theData  the String representation of the Data value of the item being checked. Only used when dealing with a typeID format of theItem, otherwise can be null. 
	 * @param  theAmount  the String representation of the Integer amount of the item to check for. If null, 1 is assumed.
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkHand(Player thePlayer, String theItem, String theData, String theAmount, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)HOLDING [ITEM_NAME|#:#] [#]
		 */

		if (theAmount == null) theAmount = "1";

		try {

			/* Check TypeId/Data */
			if (Character.isDigit(theItem.charAt(0))) {

				/* No item Data to check against */

				if (theData == null) {
					if (thePlayer.getItemInHand().getTypeId() == Integer.valueOf(theItem)
							&& thePlayer.getItemInHand().getAmount() >= Integer.valueOf(theAmount)) 
						outcome = true;
				}

				/* theData has item Data to check against */

				else {
					if (thePlayer.getItemInHand().getTypeId() == Integer.valueOf(theItem)
							&& thePlayer.getItemInHand().getAmount() >= Integer.valueOf(theAmount)
							&& String.valueOf(thePlayer.getItemInHand().getData()).equals(theData)) 
						outcome = true;

				}
			}

			/* Just check Material */
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






	/**
	 * Checks the armor of the player against a given value. 
	 *
	 * @param  thePlayer  the Player to check funds of.
	 * @param  theArmor  the String representation of the item to check for. 
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkArmor(Player thePlayer, String theArmor, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)WEARING [ITEM_NAME|#]
		 */

		try {

			ItemStack[] ArmorContents = thePlayer.getInventory().getArmorContents();

			if (Character.isDigit(theArmor.charAt(0))) {
				for (ItemStack ArmorPiece : ArmorContents) {
					if (ArmorPiece != null) {
						if (ArmorPiece.getTypeId() == Integer.valueOf(theArmor)) {
							outcome = true;
						}
					}					
				}	
			}

			else {
				for (ItemStack ArmorPiece : ArmorContents) {
					if (ArmorPiece != null) {
						if (ArmorPiece.getType() == Material.getMaterial(theArmor.toUpperCase())) {
							outcome = true;
						}
					}					
				}
			}

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured. Check your command syntax.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}





	/**
	 * Checks the potion effects of the player against a given value. 
	 *
	 * @param  thePlayer  the Player to check funds of.
	 * @param  theEffects  the List<String> of the potion effects to check for. 
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkEffects(Player thePlayer, List<String> theEffects, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)POTIONEFFECT [POTION_EFFECT] (POTION_EFFECT)
		 */

		try {

			for (String theEffect : theEffects) {
				if (theEffect != null) {
					if (thePlayer.hasPotionEffect(PotionEffectType.getByName(theEffect))) outcome = true;
				}
			}					

		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}

	
	
	
	
	/**
	 * Checks the permissions of the player against a given value with Vault. 
	 *
	 * @param  thePlayer  the Player to check funds of.
	 * @param  thePermissions  the List<String> of the permission nodes to check for. 
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkPermissions(Player thePlayer, List<String> thePermissions, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)PERMISSION [List of permission.nodes]
		 */

		try {

			for (String thePermission : thePermissions) {
				if (thePermission != null) {
					if (thePlayer.hasPermission(thePermission)) outcome = true;
				}
			}	
			
		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured. Check your command syntax.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}
	
	
	
	
	
	
	/**
	 * Checks the permissions of the player against a given value with Vault. 
	 *
	 * @param  thePlayer  the Player to check funds of.
	 * @param  thePermissions  the List<String> of the permission nodes to check for. 
	 * @param  negativeRequirement  Set to true if this is a negative (-) requirement.
	 * @return Whether the conditions were met or failed.
	 */

	public boolean checkGroups(Player thePlayer, List<String> theGroups, boolean negativeRequirement) {

		boolean outcome = false;

		/*
		 * (-)GROUP [List of Group names]
		 */

		try {

			for (String theGroup : theGroups) {
				if (theGroup != null) {
					if (Denizen.denizenPerms.playerInGroup(thePlayer, theGroup)
				|| Denizen.denizenPerms.playerInGroup(thePlayer.getWorld(), thePlayer.getName(), theGroup)) outcome = true;
				}
			}	
			
		} catch(Throwable error) {
			Bukkit.getLogger().info("Denizen: An error has occured. Check your command syntax.");
			Bukkit.getLogger().info("--- Error follows: " + error);
		}

		if (negativeRequirement != outcome) return true;

		return false;
	}
	
}