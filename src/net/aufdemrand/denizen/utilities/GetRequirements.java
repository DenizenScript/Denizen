package net.aufdemrand.denizen.utilities;

import java.lang.reflect.Array;
import java.util.ArrayList;
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
		
		/* No requirements met yet! */
		int numberMet = 0; 
		boolean negativeRequirement;
		
		/* Requirement node "NONE"? No requirements in the LIST? No need to continue, return TRUE */
		if (requirementMode.equalsIgnoreCase("NONE") || requirementList.isEmpty()) return true;

		for (String requirementEntry : requirementList) {

			/* Check if this is a Negative Requirement */
			if (requirementEntry.startsWith("-")) { 
				negativeRequirement = true; 
				requirementEntry = requirementEntry.substring(1); 
			}
			else negativeRequirement = false;

			String[] arguments = new String[25];
			arguments = requirementEntry.split(" ");

			switch (Requirement.valueOf(arguments[0].toUpperCase())) {

			case NONE:
				return true;

			case TIME: // (-)TIME [DAWN|DAY|DUSK|NIGHT]  or  (-)TIME [#] [#]
				if (Denizen.getWorld.checkTime(theEntity.getWorld(), arguments[1], arguments[2], negativeRequirement)) numberMet++;
				break;

			case STORMING:	case STORMY:  case PRECIPITATING:  case PRECIPITATION:  // (-)PRECIPITATION
				if (Denizen.getWorld.checkWeather(theEntity.getWorld(), "PRECIPITATION", negativeRequirement)) numberMet++;
				break;

			case SUNNY:  // (-)SUNNY
				if (Denizen.getWorld.checkWeather(theEntity.getWorld(), "SUNNY", negativeRequirement)) numberMet++;
				break;

			case HUNGER:  // (-)HUNGER [FULL|HUNGRY|STARVING]
				if (Denizen.getPlayer.checkSaturation((Player) theEntity, arguments[1], negativeRequirement)) numberMet++;
				break;
				
			case LEVEL:  // (-)LEVEL [#] (#)
				if (Denizen.getPlayer.checkLevel((Player) theEntity, arguments[1], arguments[2], negativeRequirement)) numberMet++;
				break;

			case WORLD:  // (-)WORLD [List of Worlds]
				List<String> theWorlds = Arrays.asList(arguments);
				theWorlds.remove(0);   /* Remove the command from arguments */
				if (Denizen.getWorld.checkWorld(theEntity, theWorlds, negativeRequirement)) numberMet++;
				break;

			case NAME:  // (-)Name [List of Names]
				List<String> theNames = Arrays.asList(arguments);
				theNames.remove(0);   /* Remove the command from arguments */
				if (Denizen.getPlayer.checkName((Player) theEntity, theNames, negativeRequirement)) numberMet++;
				break;
	
			case MONEY: // (-)MONEY [Amount of Money, or more]
				if (Denizen.getPlayer.checkFunds((Player) theEntity, arguments[1], negativeRequirement)) numberMet++;
				break;
				
			case ITEM: // (-)ITEM [ITEM_NAME] (# of that item, or more) (ENCHANTMENT_TYPE)
				if (Denizen.getPlayer.checkInventory((Player) theEntity, arguments[1], arguments[2], negativeRequirement)) numberMet++;
				break;
				
			case HOLDING: // (-)HOLDING [ITEM_NAME] (ENCHANTMENT_TYPE)
				if (Denizen.getPlayer.checkHand((Player) theEntity, arguments[1], negativeRequirement)) numberMet++;
				break;
				
				
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
				if (Denizen.getPlayer.checkEquipment((Player) theEntity, arguments[1], negativeRequirement)) numberMet++;
				break;
				
				
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
