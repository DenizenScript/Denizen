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
import org.bukkit.entity.LivingEntity;
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




	public boolean check(String theScript, LivingEntity theEntity, boolean isPlayer) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		String requirementMode = plugin.getScripts().getString("" + theScript + ".Requirements.Mode");
		List<String> requirementList = plugin.getScripts().getStringList("" + theScript + ".Requirements.List");
		int numberMet = 0;
		boolean negativeRequirement;
		
		/* DEPRECATED, will be deleted when reformat no longer requires it. Use (Player) theEntity instead. */
		Player thePlayer = null;
		if (isPlayer) thePlayer = (Player) theEntity;
		/* ---------- */
		
		/* Requirement node "NONE"? No requirements in the LIST? No need to continue, return TRUE */
		if (requirementMode.equalsIgnoreCase("NONE") 
				|| requirementList.isEmpty()) return true;

		for (String requirementEntry : requirementList) {

			/* Check if this is a Negative Requirement */
			if (requirementEntry.startsWith("-")) { 
				negativeRequirement = true; 
				requirementEntry = requirementEntry.substring(1); 
			}
			else negativeRequirement = false;

			/* DEPRECATED, will be deleted when reformat no longer requires it. Use arguments instead. */
			String[] splitArgs = requirementEntry.split(" ", 2); 
			/* ---------- */
			String[] arguments = new String[25];
			arguments = requirementEntry.split(" ");


			switch (Requirement.valueOf(arguments[0].toUpperCase())) {

			case NONE:
				return true;

			case TIME: // (-)TIME [DAWN|DAY|DUSK|NIGHT]  or  (-)TIME [#] [#]
				if (Denizen.getWorld.checkTime(theEntity.getWorld(), arguments[1], arguments[2], negativeRequirement)) numberMet++;
				break;

			case STORMING:	case STORMY:  case PRECIPITATING:  case PRECIPITATION:  // (-)PRECIPITATING
				if (Denizen.getWorld.checkWeather(theEntity.getWorld(), "PRECIPITATION", negativeRequirement)) numberMet++;
				break;

			case SUNNY:  // (-)SUNNY    - Negative would trigger on Raining or Storming
				if (Denizen.getWorld.checkWeather(theEntity.getWorld(), "SUNNY", negativeRequirement)) numberMet++;
				break;

			case HUNGER:  // (-)HUNGER [FULL|HUNGRY|STARVING]
				if (Denizen.getPlayer.checkSaturation(thePlayer, arguments[1], negativeRequirement)) numberMet++;
				break;
				
			case LEVEL:  // (-)LEVEL [#] (#)
				if (Denizen.getPlayer.checkLevel(thePlayer, arguments[1], arguments[2], negativeRequirement)) numberMet++;
				break;


			case WORLD:  // (-)WORLD [World Name] [or this World Name] [or this World...]
				String[] theseWorlds = splitArgs[1].split(" ");
				if (negativeRequirement) {
					boolean tempMet = true;
					for (String thisWorld : theseWorlds) {
						if (thePlayer.getWorld().getName().equalsIgnoreCase(thisWorld)) tempMet = false;
					}
					if (tempMet) numberMet++;
				} else {
					for (String thisWorld : theseWorlds) {
						if (thePlayer.getWorld().getName().equalsIgnoreCase(thisWorld)) numberMet++;
					}
				}
				break;

			case NAME:  // (-)Name [Name] [or this Name] [or this Name, etc...]
				String[] theseNames = splitArgs[1].split(" ");
				if (negativeRequirement) {
					boolean tempMet = true;
					for (String thisName : theseNames) {
						if (thePlayer.getName().equalsIgnoreCase(thisName)) tempMet = false;
					}
					if (tempMet) numberMet++;
				} else {
					for (String thisName : theseNames) {
						if (thePlayer.getName().equalsIgnoreCase(thisName)) numberMet++;
					}
				}
				break;


			case MONEY: // (-)MONEY [Amount of Money, or more]
				if (negativeRequirement) { if (!Denizen.denizenEcon.has(thePlayer.getName(), Integer.parseInt(splitArgs[1]))){ numberMet++;} }
				else if (Denizen.denizenEcon.has(thePlayer.getName(), Integer.parseInt(splitArgs[1]))) {numberMet++;}
				break;



			case ITEM: // (-)ITEM [ITEM_NAME] (# of that item, or more) (ENCHANTMENT_TYPE)

				String[] theseItemArgs = splitArgs[1].split(" ");
				int itemAmt = 1;
				if (theseItemArgs.length >= 2) itemAmt = Integer.parseInt(theseItemArgs[1]);
				Material thisItem = Material.valueOf((theseItemArgs[0]));
				Map<Material, Integer> PlayerInv = new HashMap<Material, Integer>();
				Map<Material, Boolean> isEnchanted = new HashMap<Material, Boolean>();
				ItemStack[] getContentsArray = thePlayer.getInventory().getContents();
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

				if (negativeRequirement) {
					if (!PlayerInv.containsKey(thisItem)) numberMet++;
					if (PlayerInv.containsKey(thisItem) && theseItemArgs.length < 3) {
						if (PlayerInv.get(thisItem) < itemAmt) {numberMet++; }
					}
					else if (PlayerInv.containsKey(thisItem) && isEnchanted.get(thisItem)) {
						if (PlayerInv.get(thisItem) < itemAmt) numberMet++; }
				}
				else {
					if (PlayerInv.containsKey(thisItem) && theseItemArgs.length < 3) {
						if (PlayerInv.get(thisItem) >= itemAmt) { numberMet++; } }
					else if (PlayerInv.containsKey(thisItem) && isEnchanted.get(thisItem)) {
						if (PlayerInv.get(thisItem) >= itemAmt) numberMet++;}
				}
				break;

			case HOLDING: // (-)HOLDING [ITEM_NAME] (ENCHANTMENT_TYPE)
				String[] itemArgs = splitArgs[1].split(" ");
				if (negativeRequirement) {if (thePlayer.getItemInHand().getType() != Material.getMaterial(itemArgs[0])) {
					if (itemArgs.length == 1) numberMet++;
					else if (!thePlayer.getItemInHand().getEnchantments().containsKey(Enchantment.getByName(itemArgs[1])))
						numberMet++;}
				} else if (thePlayer.getItemInHand().getType() == Material.getMaterial(itemArgs[0])) {
					if (itemArgs.length == 1) numberMet++;
					else if (thePlayer.getItemInHand().getEnchantments().containsKey(Enchantment.getByName(itemArgs[1])))
						numberMet++;
				}
				break;


			case WEARING:
				String[] wearingArgs = splitArgs[1].split(" ");

				ItemStack[] ArmorContents = thePlayer.getInventory().getArmorContents();
				Boolean match = false;

				for (ItemStack ArmorPiece : ArmorContents) {

					if (ArmorPiece != null) {
						if (ArmorPiece.getType() == Material.getMaterial(wearingArgs[0].toUpperCase())) {
							match = true;
						}
					}					
				}

				if (match && !negativeRequirement) numberMet++;
				if (!match && negativeRequirement) numberMet++;

				break;

			case POTIONEFFECT: // (-)POTIONEFFECT [POTION_EFFECT_TYPE]
				if (negativeRequirement) {if (!thePlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) numberMet++;}
				else if (thePlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) numberMet++;
				break;

			case FINISHED:
			case SCRIPT: // (-)SCRIPT [Script Name]
				if (negativeRequirement) { if (!Denizen.getScript.getScriptComplete(thePlayer, splitArgs[1])) numberMet++; }
				else if (Denizen.getScript.getScriptComplete(thePlayer, splitArgs[1])) numberMet++;
				break;

			case FAILED: // (-)SCRIPT [Script Name]
				if (negativeRequirement) { if (!Denizen.getScript.getScriptFail(thePlayer, splitArgs[1])) numberMet++; }
				else if (Denizen.getScript.getScriptFail(thePlayer, splitArgs[1])) numberMet++;
				break;


			case GROUP:
				if (negativeRequirement) { if (!Denizen.denizenPerms.playerInGroup(thePlayer.getWorld(), thePlayer.getName(),	splitArgs[1])) numberMet++; }
				else if (Denizen.denizenPerms.playerInGroup(thePlayer.getWorld(), thePlayer.getName(), splitArgs[1])) numberMet++;
				break;

			case PERMISSION:  // (-)PERMISSION [this.permission.node]
				if (negativeRequirement) { if (!Denizen.denizenPerms.playerHas(thePlayer.getWorld(), thePlayer.getName(),	splitArgs[1])) numberMet++; }
				else if (Denizen.denizenPerms.playerHas(thePlayer.getWorld(), thePlayer.getName(), splitArgs[1])) numberMet++;
				break;
			}
		}

		if (requirementMode.equalsIgnoreCase("ALL") 
				&& numberMet == requirementList.size()) return true;

		String[] ModeArgs = requirementMode.split(" ");
		if (ModeArgs[0].equalsIgnoreCase("ANY") 
				&& numberMet >= Integer.parseInt(ModeArgs[1])) return true;

		/* Nothing met, return FALSE */	
		return false;

	}





}
