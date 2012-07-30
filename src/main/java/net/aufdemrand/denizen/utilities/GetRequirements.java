package net.aufdemrand.denizen.utilities;

import java.util.LinkedList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.command.core.FailCommand;
import net.aufdemrand.denizen.command.core.FinishCommand;
import net.citizensnpcs.command.exception.RequirementMissingException;

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
		STORMING, DURABILITY
	}


	private Denizen plugin;

	public GetRequirements(Denizen denizen) {
		plugin = denizen;
	}




	public boolean check(String theScript, LivingEntity theEntity, boolean isPlayer) throws RequirementMissingException {

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
			String[] argumentPopulator = requirementEntry.split(" ");

			for (int count = 0; count < 25; count++) {
				if (argumentPopulator.length > count) arguments[count] = argumentPopulator[count];
				else arguments[count] = null;
			}

			try {
			
			switch (Requirement.valueOf(arguments[0].toUpperCase())) {

			case NONE:
				return true;

			case TIME: // (-)TIME [DAWN|DAY|DUSK|NIGHT]  or  (-)TIME [#] [#]
				if (plugin.getWorld.checkTime(theEntity.getWorld(), arguments[1], arguments[2], negativeRequirement)) numberMet++;
				break;

			case STORMING:	case STORMY:  case PRECIPITATING:  case PRECIPITATION:  // (-)PRECIPITATION
				if (plugin.getWorld.checkWeather(theEntity.getWorld(), "PRECIPITATION", negativeRequirement)) numberMet++;
				break;

			case SUNNY:  // (-)SUNNY
				if (plugin.getWorld.checkWeather(theEntity.getWorld(), "SUNNY", negativeRequirement)) numberMet++;
				break;

			case HUNGER:  // (-)HUNGER [FULL|HUNGRY|STARVING]
				if (plugin.getPlayer.checkSaturation((Player) theEntity, arguments[1], negativeRequirement)) numberMet++;
				break;

			case LEVEL:  // (-)LEVEL [#] (#)
				if (plugin.getPlayer.checkLevel((Player) theEntity, arguments[1], arguments[2], negativeRequirement)) numberMet++;
				break;

			case WORLD:  // (-)WORLD [List of Worlds]
				List<String> theWorlds = new LinkedList<String>(); // = Arrays.asList(arguments);
				for(String arg : arguments) if (arg != null) theWorlds.add(arg.toUpperCase());
				theWorlds.remove(0);   /* Remove the command from the list */
				if (plugin.getWorld.checkWorld(theEntity, theWorlds, negativeRequirement)) numberMet++;
				break;

			case NAME:  // (-)Name [List of Names]
				List<String> theNames = new LinkedList<String>(); // = Arrays.asList(arguments);
				for(String arg : arguments) if (arg != null) theNames.add(arg.toUpperCase());
				theNames.remove(0);   /* Remove the command from the list */
				if (plugin.getPlayer.checkName((Player) theEntity, theNames, negativeRequirement)) numberMet++;
				break;

			case MONEY: // (-)MONEY [# or more]
				if (plugin.getPlayer.checkFunds((Player) theEntity, arguments[1], negativeRequirement)) numberMet++;
				break;

			case ITEM: // (-)ITEM [ITEM_NAME|#:#] (# or more)
				String[] itemArgs = splitItem(arguments[1]);
				if (plugin.getPlayer.checkInventory((Player) theEntity, itemArgs[0], itemArgs[1], arguments[2], negativeRequirement)) numberMet++;
				break;

			case HOLDING: // (-)HOLDING [ITEM_NAME|#:#] (# or more)
				String[] holdingArgs = splitItem(arguments[1]);
				if (plugin.getPlayer.checkHand((Player) theEntity, holdingArgs[0], holdingArgs[1], arguments[2], negativeRequirement)) numberMet++;
				break;

			case WEARING: // (-) WEARING [ITEM_NAME|#]
				if (plugin.getPlayer.checkArmor((Player) theEntity, arguments[1], negativeRequirement)) numberMet++;
				break;

			case POTIONEFFECT: // (-)POTIONEFFECT [List of POITION_TYPESs]
				List<String> thePotions = new LinkedList<String>(); // = Arrays.asList(arguments);
				for(String arg : arguments) if (arg != null) thePotions.add(arg.toUpperCase());
				thePotions.remove(0);   /* Remove the command from the list */
				if (plugin.getPlayer.checkEffects((Player) theEntity, thePotions, negativeRequirement)) numberMet++;
				break;

			case FINISHED:
			case SCRIPT: // (-)FINISHED (#) [Script Name]
				if (plugin.getCommandRegistry().getCommand(FinishCommand.class).getScriptCompletes((Player) theEntity, requirementEntry.split(" ", 2)[1], requirementEntry.split(" ", 3)[1], negativeRequirement)) numberMet++;
				break;

			case FAILED: // (-)SCRIPT [Script Name]
				if (plugin.getCommandRegistry().getCommand(FailCommand.class).getScriptFail((Player) theEntity, requirementEntry.split(" ", 2)[1], negativeRequirement)) numberMet++;
				break;

			case GROUP:
				List<String> theGroups = new LinkedList<String>(); // = Arrays.asList(arguments);
				for(String arg : arguments) if (arg != null) theGroups.add(arg);
				theGroups.remove(0);   /* Remove the command from the list */
				if (plugin.getPlayer.checkGroups((Player) theEntity, theGroups, negativeRequirement)) numberMet++;
				break;

			case PERMISSION:  // (-)PERMISSION [this.permission.node]
				List<String> thePermissions = new LinkedList<String>(); // = Arrays.asList(arguments);
				for(String arg : arguments) if (arg != null) thePermissions.add(arg);
				thePermissions.remove(0);   /* Remove the command from the list */
				if (plugin.getPlayer.checkPermissions((Player) theEntity, thePermissions, negativeRequirement)) numberMet++;
				break;

			case DURABILITY:  // (-)DURABILITY [>,<,=] [#|#%]
				if (plugin.getPlayer.checkDurability((Player) theEntity, arguments[1], arguments[2], negativeRequirement)) numberMet++;
				break;
			}
			
			} catch (Throwable e) {
				throw new RequirementMissingException(e.getMessage());
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




	/* 
	 * Converts a string with the format #:# (TypeId:Data) to a String[] 
	 * 
	 * Element [0] -- TypeId
	 * Element [1] -- Data
	 */

	public String[] splitItem(String theItemWithData) {

		String[] itemArgs = new String[2];
		if (theItemWithData.split(":", 2).length == 1) {
			itemArgs[0] = theItemWithData;
			itemArgs[1] = null;

		}
		else {
			itemArgs[0] = theItemWithData.split(":", 2)[0];
			itemArgs[1] = theItemWithData.split(":", 2)[1];
		}

		return itemArgs;
	}



}
