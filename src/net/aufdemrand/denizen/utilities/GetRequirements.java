package net.aufdemrand.denizen.utilities;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.Denizen;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class GetRequirements {
	
	
	
	public enum RequirementMode {
		NONE, ALL, ANY
	}

	public enum Requirement {
		NONE, NAME, WEARING, ITEM, HOLDING, TIME, PRECIPITATION, ACTIVITY, FINISHED, SCRIPT, FAILED,
		STORMY, SUNNY, HUNGER, WORLD, PERMISSION, LEVEL, GROUP, MONEY, POTIONEFFECT, PRECIPITATING,
		STORMING 
	}

	
	
	
	public boolean check(String thisScript, Player thisPlayer) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		
		String RequirementsMode = plugin.getScripts().getString("" + thisScript + ".Requirements.Mode");

		if (RequirementsMode.equalsIgnoreCase("none")) return true;
		List<String> RequirementsList = plugin.getScripts().getStringList("" + thisScript
				+ ".Requirements.List");
		if (RequirementsList.isEmpty()) { return false; }
		int MetReqs = 0;
		boolean negReq;
		for (String RequirementArgs : RequirementsList) {
			if (RequirementArgs.startsWith("-")) { negReq = true; RequirementArgs = RequirementArgs.substring(1); }
			else negReq = false;
			String[] splitArgs = RequirementArgs.split(" ", 2);
			switch (Requirement.valueOf(splitArgs[0].toUpperCase())) {

			case NONE:
				return true;

			case TIME: // (-)TIME DAY   or  (-)TIME NIGHT    Note: DAY = 0, NIGHT = 16000
				// or (-)TIME [At least this Time 0-23999] [But no more than this Time 1-24000]

				if (negReq) {
					if (splitArgs[1].equalsIgnoreCase("DAY") && thisPlayer.getWorld().getTime() > 16000)
					{ MetReqs++; break; }
					if (splitArgs[1].equalsIgnoreCase("NIGHT") && thisPlayer.getWorld().getTime() < 16000)
					{ MetReqs++; break; }
					if (!splitArgs[1].equalsIgnoreCase("DAY") && !splitArgs[1].equalsIgnoreCase("NIGHT")) {
						String[] theseTimes = splitArgs[1].split(" ");
						if (thisPlayer.getWorld().getTime() < Integer.parseInt(theseTimes[0]) && thisPlayer.
								getWorld().getTime() > Integer.parseInt(theseTimes[1])) MetReqs++;
					}
				} else {
					if (splitArgs[1].equalsIgnoreCase("DAY") && thisPlayer.getWorld().getTime() < 16000)
					{ MetReqs++; break; }
					if (splitArgs[1].equalsIgnoreCase("NIGHT") && thisPlayer.getWorld().getTime() > 16000)
					{ MetReqs++; break; }
					if (!splitArgs[1].equalsIgnoreCase("DAY") && !splitArgs[1].equalsIgnoreCase("NIGHT")) {
						String[] theseTimes = splitArgs[1].split(" ");
						if (thisPlayer.getWorld().getTime() >= Integer.parseInt(theseTimes[0]) && thisPlayer.
								getWorld().getTime() <= Integer.parseInt(theseTimes[1])) MetReqs++;
					}
				}
				break;



			case STORMING:
			case STORMY:
			case PRECIPITATING:
			case PRECIPITATION:  // (-)PRECIPITATION

				if (negReq) {
					if (!thisPlayer.getWorld().hasStorm()) MetReqs++;
				}
				else if (thisPlayer.getWorld().hasStorm()) MetReqs++;
				break;


			case SUNNY:  // (-)SUNNY    - Negative would trigger on Raining or Storming
				if (negReq) if (thisPlayer.getWorld().hasStorm()) MetReqs++;
				else if (!thisPlayer.getWorld().hasStorm()) MetReqs++;
				break;




			case HUNGER:  // (-)HUNGER FULL  or  (-)HUNGER HUNGRY  or  (-)HUNGER STARVING
				if (negReq) {
					if (splitArgs[1].equalsIgnoreCase("FULL")) if (thisPlayer.getFoodLevel() < 20) MetReqs++;
					if (splitArgs[1].equalsIgnoreCase("HUNGRY")) if (thisPlayer.getFoodLevel() >= 20) MetReqs++;
					if (splitArgs[1].equalsIgnoreCase("STARVING")) if (thisPlayer.getFoodLevel() > 1) MetReqs++;
				} else {
					if (splitArgs[1].equalsIgnoreCase("FULL")) if (thisPlayer.getFoodLevel() >= 20) MetReqs++;
					if (splitArgs[1].equalsIgnoreCase("HUNGRY")) if (thisPlayer.getFoodLevel() < 18) MetReqs++;
					if (splitArgs[1].equalsIgnoreCase("STARVING")) if (thisPlayer.getFoodLevel() < 1) MetReqs++;
				}
				break;

			case LEVEL:  // (-)LEVEL [This Level # or higher]
				// or  (-)LEVEL [At least this Level #] [But no more than this Level #]
				if (negReq) {
					if (Array.getLength(splitArgs[1].split(" ")) == 1) {
						if (thisPlayer.getLevel() < Integer.parseInt(splitArgs[1])) MetReqs++;
					} else {
						String[] theseLevels = splitArgs[1].split(" ");
						if (thisPlayer.getLevel() < Integer.parseInt(theseLevels[0]) && thisPlayer.getLevel()
								> Integer.parseInt(theseLevels[1])) MetReqs++;
					}
				} else {
					if (Array.getLength(splitArgs[1].split(" ")) == 1) {
						if (thisPlayer.getLevel() >= Integer.parseInt(splitArgs[1])) MetReqs++;
					} else {
						String[] theseLevels = splitArgs[1].split(" ");
						if (thisPlayer.getLevel() >= Integer.parseInt(theseLevels[0]) && thisPlayer.getLevel()
								<= Integer.parseInt(theseLevels[1])) MetReqs++;
					}
				}
				break;

	
			case WORLD:  // (-)WORLD [World Name] [or this World Name] [or this World...]
				String[] theseWorlds = splitArgs[1].split(" ");
				if (negReq) {
					boolean tempMet = true;
					for (String thisWorld : theseWorlds) {
						if (thisPlayer.getWorld().getName().equalsIgnoreCase(thisWorld)) tempMet = false;
					}
					if (tempMet) MetReqs++;
				} else {
					for (String thisWorld : theseWorlds) {
						if (thisPlayer.getWorld().getName().equalsIgnoreCase(thisWorld)) MetReqs++;
					}
				}
				break;

			case NAME:  // (-)Name [Name] [or this Name] [or this Name, etc...]
				String[] theseNames = splitArgs[1].split(" ");
				if (negReq) {
					boolean tempMet = true;
					for (String thisName : theseNames) {
						if (thisPlayer.getName().equalsIgnoreCase(thisName)) tempMet = false;
					}
					if (tempMet) MetReqs++;
				} else {
					for (String thisName : theseNames) {
						if (thisPlayer.getName().equalsIgnoreCase(thisName)) MetReqs++;
					}
				}
				break;


			case MONEY: // (-)MONEY [Amount of Money, or more]
				if (negReq) { if (!Denizen.denizenEcon.has(thisPlayer.getName(), Integer.parseInt(splitArgs[1]))){ MetReqs++;} }
				else if (Denizen.denizenEcon.has(thisPlayer.getName(), Integer.parseInt(splitArgs[1]))) {MetReqs++;}
				break;



			case ITEM: // (-)ITEM [ITEM_NAME] (# of that item, or more) (ENCHANTMENT_TYPE)

				String[] theseItemArgs = splitArgs[1].split(" ");
				int itemAmt = 1;
				if (theseItemArgs.length >= 2) itemAmt = Integer.parseInt(theseItemArgs[1]);
				Material thisItem = Material.valueOf((theseItemArgs[0]));
				Map<Material, Integer> PlayerInv = new HashMap<Material, Integer>();
				Map<Material, Boolean> isEnchanted = new HashMap<Material, Boolean>();
				ItemStack[] getContentsArray = thisPlayer.getInventory().getContents();
				List<ItemStack> getContents = Arrays.asList(getContentsArray);
				for (int x=0; x < getContents.size(); x++) {
					if (getContents.get(x) != null) {
						if (PlayerInv.containsKey(getContents.get(x).getType())) {
							int t = PlayerInv.get(getContents.get(x).getType());
							t = t + getContents.get(x).getAmount(); PlayerInv.put(getContents.get(x).getType(), t);
						}
						else PlayerInv.put(getContents.get(x).getType(), getContents.get(x).getAmount());
						if (theseItemArgs.length >= 3) {
							if (getContents.get(x).containsEnchantment(Enchantment.getByName(theseItemArgs[2])))
								isEnchanted.put(getContents.get(x).getType(), true); }
					}
				}

				if (negReq) {
					if (!PlayerInv.containsKey(thisItem)) MetReqs++;
					if (PlayerInv.containsKey(thisItem) && theseItemArgs.length < 3) {
						if (PlayerInv.get(thisItem) < itemAmt) {MetReqs++; }
					}
					else if (PlayerInv.containsKey(thisItem) && isEnchanted.get(thisItem)) {
						if (PlayerInv.get(thisItem) < itemAmt) MetReqs++; }
				}
				else {
					if (PlayerInv.containsKey(thisItem) && theseItemArgs.length < 3) {
						if (PlayerInv.get(thisItem) >= itemAmt) { MetReqs++; } }
					else if (PlayerInv.containsKey(thisItem) && isEnchanted.get(thisItem)) {
						if (PlayerInv.get(thisItem) >= itemAmt) MetReqs++;}
				}
				break;

			case HOLDING: // (-)HOLDING [ITEM_NAME] (ENCHANTMENT_TYPE)
				String[] itemArgs = splitArgs[1].split(" ");
				if (negReq) {if (thisPlayer.getItemInHand().getType() != Material.getMaterial(itemArgs[0])) {
					if (itemArgs.length == 1) MetReqs++;
					else if (!thisPlayer.getItemInHand().getEnchantments().containsKey(Enchantment.getByName(itemArgs[1])))
						MetReqs++;}
				} else if (thisPlayer.getItemInHand().getType() == Material.getMaterial(itemArgs[0])) {
					if (itemArgs.length == 1) MetReqs++;
					else if (thisPlayer.getItemInHand().getEnchantments().containsKey(Enchantment.getByName(itemArgs[1])))
						MetReqs++;
				}
				break;


			case WEARING:
				String[] wearingArgs = splitArgs[1].split(" ");

				ItemStack[] ArmorContents = thisPlayer.getInventory().getArmorContents();
				Boolean match = false;

				for (ItemStack ArmorPiece : ArmorContents) {

					if (ArmorPiece != null) {
						if (ArmorPiece.getType() == Material.getMaterial(wearingArgs[0].toUpperCase())) {
							match = true;
						}
					}					
				}

				if (match && !negReq) MetReqs++;
				if (!match && negReq) MetReqs++;

				break;

			case POTIONEFFECT: // (-)POTIONEFFECT [POTION_EFFECT_TYPE]
				if (negReq) {if (!thisPlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) MetReqs++;}
				else if (thisPlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) MetReqs++;
				break;

			case FINISHED:
			case SCRIPT: // (-)SCRIPT [Script Name]
				if (negReq) { if (!Denizen.getScript.getScriptComplete(thisPlayer, splitArgs[1])) MetReqs++; }
				else if (Denizen.getScript.getScriptComplete(thisPlayer, splitArgs[1])) MetReqs++;
				break;

			case FAILED: // (-)SCRIPT [Script Name]
				if (negReq) { if (!Denizen.getScript.getScriptFail(thisPlayer, splitArgs[1])) MetReqs++; }
				else if (Denizen.getScript.getScriptFail(thisPlayer, splitArgs[1])) MetReqs++;
				break;

				
			case GROUP:
				if (negReq) { if (!Denizen.denizenPerms.playerInGroup(thisPlayer.getWorld(), thisPlayer.getName(),	splitArgs[1])) MetReqs++; }
				else if (Denizen.denizenPerms.playerInGroup(thisPlayer.getWorld(), thisPlayer.getName(), splitArgs[1])) MetReqs++;
				break;

			case PERMISSION:  // (-)PERMISSION [this.permission.node]
				if (negReq) { if (!Denizen.denizenPerms.playerHas(thisPlayer.getWorld(), thisPlayer.getName(),	splitArgs[1])) MetReqs++; }
				else if (Denizen.denizenPerms.playerHas(thisPlayer.getWorld(), thisPlayer.getName(), splitArgs[1])) MetReqs++;
				break;
			}
		}
		if (RequirementsMode.equalsIgnoreCase("all") && MetReqs == RequirementsList.size()) return true;
		String[] ModeArgs = RequirementsMode.split(" ");
		if (ModeArgs[0].equalsIgnoreCase("any") && MetReqs >= Integer.parseInt(ModeArgs[1])) return true;

		return false;
	}
	
}
