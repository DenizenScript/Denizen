package net.aufdemrand.denizen.utilities;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GetRequirements {



	public enum RequirementMode {
		NONE, ALL, ANY
	}

	public enum Requirement {
		NONE, NAME, WEARING, ITEM, HOLDING, TIME, PRECIPITATION, ACTIVITY, FINISHED, SCRIPT, FAILED,
		STORMY, SUNNY, HUNGER, WORLD, PERMISSION, LEVEL, GROUP, MONEY, POTIONEFFECT, PRECIPITATING,
		STORMING 
	}




	@SuppressWarnings("null")
	public boolean check(String theScript, LivingEntity theEntity, boolean isPlayer) {

		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		String requirementMode = plugin.getScripts().getString(theScript + ".Requirements.Mode");
		List<String> requirementList = plugin.getScripts().getStringList(theScript + ".Requirements.List");
		
		/* No requirements met yet, we just started! */
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
				theWorlds.remove(0);   /* Remove the command from the list */
				if (Denizen.getWorld.checkWorld(theEntity, theWorlds, negativeRequirement)) numberMet++;
				break;

			case NAME:  // (-)Name [List of Names]
				List<String> theNames = Arrays.asList(arguments);
				theNames.remove(0);   /* Remove the command from the list */
				if (Denizen.getPlayer.checkName((Player) theEntity, theNames, negativeRequirement)) numberMet++;
				break;
	
			case MONEY: // (-)MONEY [# or more]
				if (Denizen.getPlayer.checkFunds((Player) theEntity, arguments[1], negativeRequirement)) numberMet++;
				break;
				
			case ITEM: // (-)ITEM [ITEM_NAME|#:#] (# or more)
				String[] itemArgs = new String[1];
				itemArgs = arguments[1].split(":", 2);
				if (Denizen.getPlayer.checkInventory((Player) theEntity, itemArgs[0], itemArgs[1], arguments[2], negativeRequirement)) numberMet++;
				break;
				
			case HOLDING: // (-)HOLDING [ITEM_NAME|#:#] (# or more)
				String[] holdingArgs = new String[1];
				holdingArgs = arguments[1].split(":", 2);
				if (Denizen.getPlayer.checkHand((Player) theEntity, holdingArgs[0], holdingArgs[1], arguments[2], negativeRequirement)) numberMet++;
				break;
				
			case WEARING: // (-) WEARING [ITEM_NAME|#]
				if (Denizen.getPlayer.checkArmor((Player) theEntity, arguments[1], negativeRequirement)) numberMet++;
				break;

			case POTIONEFFECT: // (-)POTIONEFFECT [List of POITION_TYPESs]
				List<String> thePotions = Arrays.asList(arguments);
				thePotions.remove(0);   /* Remove the command from the list */
				if (Denizen.getPlayer.checkEffects((Player) theEntity, thePotions, negativeRequirement)) numberMet++;
				break;
				
			case FINISHED:
			case SCRIPT: // (-)FINISHED (#) [Script Name]
				if (Denizen.getScript.getScriptCompletes((Player) theEntity, requirementEntry.split(" ", 2)[1], requirementEntry.split(" ", 3)[1], negativeRequirement)) numberMet++;
				break;

			case FAILED: // (-)SCRIPT [Script Name]
				if (Denizen.getScript.getScriptFail((Player) theEntity, requirementEntry.split(" ", 2)[1], negativeRequirement))
				break;

			case GROUP:
				List<String> theGroups = new LinkedList<String>(); // = Arrays.asList(arguments);
				for(String arg : arguments) {
					theGroups.add(arg);
				}
				theGroups.remove(0);   /* Remove the command from the list */
				if (Denizen.getPlayer.checkGroups((Player) theEntity, theGroups, negativeRequirement)) numberMet++;
				break;

			case PERMISSION:  // (-)PERMISSION [this.permission.node]
				List<String> thePermissions = Arrays.asList(arguments);
				thePermissions.remove(0);   /* Remove the command from the list */
				if (Denizen.getPlayer.checkPermissions((Player) theEntity, thePermissions, negativeRequirement)) numberMet++;
				break;
			}
		}

		/* Check numberMet */	
		if (requirementMode.equalsIgnoreCase("ALL") 
				&& numberMet == requirementList.size()) return true;

		String[] ModeArgs = requirementMode.split(" ");
		if (ModeArgs[0].equalsIgnoreCase("ANY") 
				&& numberMet >= Integer.parseInt(ModeArgs[1])) return true;

		/* Nothing met, return FALSE */	
		return false;

	}





}
