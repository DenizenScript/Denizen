package net.aufdemrand.denizen;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import net.aufdemrand.denizen.Denizen;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;


public class DenizenListener implements Listener {

	Denizen plugin;
	public DenizenListener(Denizen instance) { plugin = instance; }



	public enum ScriptBehavior {
		CONTROLLED, REPEAT, LOOP;
	}

	public enum RequirementMode {
		NONE, ALL, ANY; 
	}

	public enum Requirement { NONE, NAME, WEARING, INVINSIBLE, ITEM, HOLDING, TIME, PRECIPITATION, STORMY, SUNNY, HUNGER, WORLD, PERMISSION, LEVEL, SCRIPT, NOTABLE, GROUP, MONEY, POTIONEFFECT; }

	public enum Trigger {
		CHAT, CLICK, RIGHT_CLICK, LEFT_CLICK, FINISH, START, FAIL, BOUNCED;
	}

	public enum Command {
		GIVE, TAKE, WALK, PAUSE, CHAT, WHISPER, SHOUT, NARRARATE, TELEPORT, PERMISS, EXECUTE, ZAP, BOUNCE; 
	}



	/* PlayerChatListener
	 * 
	 * Called when the player chats.  Determines if player is near a Denizen, and if so, checks if there
	 * are scripts to interact with.  Also handles the chat output for the Player talking to the Denizen.
	 * 
	 * Calls GetDenizensWithinRange, TalkToNPC, GetInteractScript, ParseScript
	 */

	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {
		
		event.getPlayer().getLocation().getWorld().getName();
		
		List<net.citizensnpcs.api.npc.NPC> DenizenList = GetDenizensWithinRange(event.getPlayer().getLocation(), event.getPlayer().getWorld(), plugin.PlayerChatRangeInBlocks);
		if (DenizenList.isEmpty()) { return; }
		event.setCancelled(true);
		for (net.citizensnpcs.api.npc.NPC thisDenizen : DenizenList) {
			TalkToNPC(thisDenizen, event.getPlayer(), event.getMessage());
			String theScript = GetInteractScript(thisDenizen, event.getPlayer());
			if (theScript.equals("none")) thisDenizen.chat(event.getPlayer(), plugin.getConfig().getString("Denizens." + thisDenizen.getId() + ".Default Texts.No Script Interact", "I have nothing to say to you at this time."));
			else if (!theScript.equals("none")) ParseScript(event.getPlayer(), GetScriptName(theScript), event.getMessage(), Trigger.CHAT);
		} event.getPlayer().is
	}



	/* GetDenizensWithinRange
	 * 
	 * Requires Player Location, Player World, Range in blocks.
	 * Compiles a list of NPCs with a character type of Denizen
	 * within range of the Player.
	 * 
	 * Returns DenizensWithinRange List<NPC>
	 */

	// Is this the best way to do this?  Should we instead use getNearbyEntities?  Probably.  Can change later if becomes an issue.

	public List<net.citizensnpcs.api.npc.NPC> GetDenizensWithinRange (Location PlayerLocation, World PlayerWorld, int Range) {

		List<net.citizensnpcs.api.npc.NPC> DenizensWithinRange = new ArrayList<net.citizensnpcs.api.npc.NPC>();
		Collection<net.citizensnpcs.api.npc.NPC> DenizenNPCs = CitizensAPI.getNPCManager().getNPCs(DenizenCharacter.class); 
		if (DenizenNPCs.isEmpty()) return DenizensWithinRange;
		List<net.citizensnpcs.api.npc.NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (int x = 0; x < DenizenList.size(); x++) {
			if (DenizenList.get(x).getBukkitEntity().getWorld().equals(PlayerWorld)) {
				if (DenizenList.get(x).getBukkitEntity().getLocation().distance(PlayerLocation) < Range) DenizensWithinRange.add(DenizenList.get(x));
			}
		}
		return DenizensWithinRange;
	}



	/* TalkToNPC
	 * 
	 * Requires the NPC Denizen, Player, and the Message to relay.
	 * Sends the message from Player to Denizen with the formatting
	 * as specified in the config.yml talk_to_npc_string.
	 * 
	 * <NPC> and <TEXT> are replaced with corresponding information.
	 */

	public void TalkToNPC(net.citizensnpcs.api.npc.NPC theDenizen, Player thePlayer, String theMessage)
	{
		thePlayer.sendMessage(plugin.TalkToNPCString.replace("<NPC>", theDenizen.getName().toString()).replace("<TEXT>", theMessage));
	}



	/* GetInteractScript
	 * 
	 * Requires the Denizen and the Player
	 * Checks the Denizens scripts and returns the script that meets requirements and has
	 * the highest weight.  If no script matches, returns "none".
	 * 
	 * Returns theScript
	 * Calls CheckRequirements
	 */

	public String GetInteractScript(net.citizensnpcs.api.npc.NPC thisDenizen, Player thisPlayer) {
		String theScript = "none";
		List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getName() + ".Scripts");
		if (ScriptList.isEmpty()) { return theScript; }
		List<String> ScriptsThatMeetRequirements = new ArrayList<String>();
		// Get scripts that meet requirements
		for (String thisScript : ScriptList) {
			String [] thisScriptArray = thisScript.split(" ", 2);
			if (CheckRequirements(thisScriptArray[1], thisPlayer) == true) { ScriptsThatMeetRequirements.add(thisScript); }
		}
		// Get highest scoring script
		if (ScriptsThatMeetRequirements.size() > 1) {

			int ScriptPriority = -1;

			for (String thisScript : ScriptsThatMeetRequirements) {
				String [] thisScriptArray = thisScript.split(" ", 2);
				if (Integer.parseInt(thisScriptArray[0]) > ScriptPriority) {ScriptPriority = Integer.parseInt(thisScriptArray[0]); theScript = thisScriptArray[1]; }
			}
		}
		else if (ScriptsThatMeetRequirements.size() == 1) { theScript = ScriptsThatMeetRequirements.get(0); }

		return theScript;
	}



	/* ParseScript
	 * 
	 * Requires the Player, the Script Name, the chat message (if Chat Trigger, otherwise send null), 
	 * and the Trigger ENUM type.
	 * Sends out methods that take action based on the Trigger types. 
	 * 
	 * case CHAT calls GetCurrentStep, GetChatTriggers, TriggerChatToQue
	 * case CLICK,RIGHT_CLICK,LEFT_CLICK calls
	 * case FINISH calls
	 * case START calls
	 * case FAIL calls
	 */


	public void ParseScript(Player thePlayer, String theScript, String theMessage,  Trigger theTrigger) {

		switch (theTrigger) {

		case CHAT:
			int CurrentStep = GetCurrentStep(thePlayer, theScript);
			List<String> ChatTriggerList = GetChatTriggers(theScript, CurrentStep);
			for (int l=0; l < ChatTriggerList.size(); l++ ) {
				if (theMessage.matches(ChatTriggerList.get(l))) {
					TriggerChatToQue(theScript, CurrentStep, l, thePlayer);
				}
			}
			return;

		case CLICK:
			// get current progression
			// send script
			return;

		case FINISH:
			// get current progressions
			// send script
			return;

		}
	}



	/* TriggerChatToQue
	 * 
	 * Requires the Script, the Current Step, the Chat Trigger to trigger, and the Player
	 * Triggers the script for the chat trigger of the step and script specified.
	 * 
	 * Calls ScriptHandler to handle the commands in the script. ScriptHandler returns any
	 * raw text that needs to be sent to the player which is put in the PlayerQue for
	 * output. 
	 */

	public void TriggerChatToQue(String theScript, int CurrentStep, int ChatTrigger, Player thePlayer) {

		List<String> CurrentPlayerQue = new ArrayList<String>();
		if (Denizen.PlayerQue.get(thePlayer) != null) CurrentPlayerQue = Denizen.PlayerQue.get(thePlayer);
		Denizen.PlayerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add
		List<String> AddedToPlayerQue = plugin.getConfig().getStringList("Scripts." + theScript + ".Progression." + CurrentStep + ".Interact.Chat Trigger." + ChatTrigger + ".Script");
		if (!AddedToPlayerQue.isEmpty()) {
			for (int entry = 0; entry < AddedToPlayerQue.size(); entry++) CurrentPlayerQue.add(AddedToPlayerQue.get(0));
		}
		ScriptHandler(theScript, CurrentStep, thePlayer, CurrentPlayerQue, Trigger.CHAT);
		Denizen.PlayerQue.put(thePlayer, CurrentPlayerQue);
		return;
	}



	public List<String> ScriptHandler(String theScript, int CurrentStep, Player thePlayer, List<String> ScriptToHandle, Trigger theTrigger) {

		for (String theCommand : ScriptToHandle) {
			String[] splitArgs = theCommand.split(" ");
			switch (Command.valueOf(splitArgs[0].toUpperCase())) {

			case ZAP:  // ZAP [Optional Step # to advance to]
				
				
				
			}

			return ScriptToHandle;
		}



	/* GetCurrentStep
	 * 
	 * Requires the Player and the Script.
	 * Reads the config.yml to find the current step that the player is on
	 * for the specified script.
	 * 
	 * Returns currentStep
	 */

	public int GetCurrentStep(Player thePlayer, String theScript) {
		int currentStep = 0;
		if (plugin.getConfig().getString("Players." + thePlayer + "." + theScript + "." + "CurrentStep") != null) currentStep =  plugin.getConfig().getInt("Players." + thePlayer + "." + theScript + "." + "CurrentStep"); 
		return currentStep;
	}

	
	/* GetScriptCompletes
	 * 
	 * Requires the Player and the Script.
	 * Reads the config.yml to find if the player has completed 
	 * the specified script.
	 * 
	 * Returns number of times script has been completed.
	 */
	
	public int GetScriptCompletes(Player thePlayer, String theScript) {
		int ScriptCompletes = 0;
		if (plugin.getConfig().getString("Players." + thePlayer + "." + theScript + "." + "Completes") != null) ScriptCompletes =  plugin.getConfig().getInt("Players." + thePlayer + "." + theScript + "." + "Completes"); 
		return ScriptCompletes;
	}
	
	
	/* GetNotableCompletion
	 * 
	 * Requires the Player and the Script.
	 * Reads the config.yml to find if the player has completed 
	 * the specified script.
	 * 
	 * Returns number of times script has been completed.
	 */
	
	public boolean GetNotableCompletion(Player thePlayer, String theNotable) {
		if (plugin.getConfig().getStringList("Notables.Players." + thePlayer + "." + theNotable).contains(theNotable)) return true;
		else return false;
	}
	

	/* GetChatTriggers
	 * 
	 * Requires the Script and the Current Step.
	 * Gets a list of Chat Triggers for the step of the script specified.
	 * Chat Triggers are words required to trigger one of the chat scripts.
	 * 
	 * Returns ChatTriggers
	 */

	public List<String> GetChatTriggers(String theScript, Integer currentStep) {
		List<String> ChatTriggers = new ArrayList<String>();
		int currentTrigger = 0;
		// Add triggers to list
		for (int x=0; currentTrigger >= 0; x++) {
			String theChatTrigger = plugin.getConfig().getString("Scripts." + theScript + ".Progression." + currentStep + ".Interact.Chat Trigger." + String.valueOf(currentTrigger) + ".Trigger");
			if (theChatTrigger != null) { ChatTriggers.add(theChatTrigger); currentTrigger = x + 1; } 
			else currentTrigger = -1;
		}
		return ChatTriggers;
	}



	/* GetScriptName
	 * 
	 * Requires the raw script entry from the config.
	 * Strips the priority number from the beginning of the script name.
	 * 
	 * Returns the Script Name
	 */

	public String GetScriptName(String thisScript) {
		if (thisScript.equals("none")) { return thisScript; }
		else {
			String [] thisScriptArray = thisScript.split(" ", 2);
			return thisScriptArray[1]; }
	}



	// CHECK REQUIREMENTS  (Checks if the requirements of a script are met when given Script/Player)

	public boolean CheckRequirements(String thisScript, Player thisPlayer) {

		String RequirementsMode = plugin.getConfig().getString("Scripts." + thisScript + ".Requirements.Mode");

		List<String> RequirementsList = plugin.getConfig().getStringList("Scripts." + thisScript + ".Requirements.List");
		if (RequirementsList.isEmpty()) { return true; }
		int MetReqs = 0;
		boolean negReq = false;
		for (String RequirementArgs : RequirementsList) {
			if (RequirementArgs.startsWith("!")) { negReq = true; RequirementArgs = RequirementArgs.substring(1); }
			else negReq = false;
			String[] splitArgs = RequirementArgs.split(" ", 2);
			switch (Requirement.valueOf(splitArgs[0].toUpperCase())) {

			case NONE:
				return true;

			case TIME: // (!)TIME DAY   or  (!)TIME NIGHT  or (!)TIME [At least this Time 0-23999] [But no more than this Time 1-24000] 
					   // DAY = 0           NIGHT = 16000
				if (negReq) {
					if (splitArgs[1].equalsIgnoreCase("DAY")) if (thisPlayer.getWorld().getTime() > 16000) MetReqs++; 
					if (splitArgs[1].equalsIgnoreCase("NIGHT")) if (thisPlayer.getWorld().getTime() < 16000) MetReqs++;
					else {
						String[] theseTimes = splitArgs[1].split(" ");
						if (thisPlayer.getWorld().getTime() < Integer.parseInt(theseTimes[0]) && thisPlayer.getWorld().getTime() > Integer.parseInt(theseTimes[1])) MetReqs++;
					}
				} else {
					if (splitArgs[1].equalsIgnoreCase("DAY")) if (thisPlayer.getWorld().getTime() < 16000) MetReqs++; 
					if (splitArgs[1].equalsIgnoreCase("NIGHT")) if (thisPlayer.getWorld().getTime() > 16000) MetReqs++;
					else {
						String[] theseTimes = splitArgs[1].split(" ");
						if (thisPlayer.getWorld().getTime() >= Integer.parseInt(theseTimes[0]) && thisPlayer.getWorld().getTime() <= Integer.parseInt(theseTimes[1])) MetReqs++;
					}
				}
				
			case PERMISSION:  // (!)PERMISSION [this.permission.node]
				if (negReq) if (!Denizen.perms.playerHas(thisPlayer.getWorld(), thisPlayer.toString(), splitArgs[1])) MetReqs++;
				else if (Denizen.perms.playerHas(thisPlayer.getWorld(), thisPlayer.toString(), splitArgs[1])) MetReqs++;		

			case PRECIPITATION:  // (!)PRECIPITATION
			    if (negReq) if (!thisPlayer.getWorld().hasStorm()) MetReqs++;
				else if (thisPlayer.getWorld().hasStorm()) MetReqs++;

			case HUNGER:  // (!)HUNGER FULL  or  (!)HUNGER HUNGRY  or  (!)HUNGER STARVING
				if (negReq) {
					if (splitArgs[1].equalsIgnoreCase("FULL")) if (thisPlayer.getFoodLevel() < 20) MetReqs++; 
					if (splitArgs[1].equalsIgnoreCase("HUNGRY")) if (thisPlayer.getFoodLevel() >= 20) MetReqs++;
					if (splitArgs[1].equalsIgnoreCase("STARVING")) if (thisPlayer.getFoodLevel() > 1) MetReqs++; 
				} else {
					if (splitArgs[1].equalsIgnoreCase("FULL")) if (thisPlayer.getFoodLevel() >= 20) MetReqs++; 
					if (splitArgs[1].equalsIgnoreCase("HUNGRY")) if (thisPlayer.getFoodLevel() < 18) MetReqs++;
					if (splitArgs[1].equalsIgnoreCase("STARVING")) if (thisPlayer.getFoodLevel() < 1) MetReqs++; 
				}

			case LEVEL:  // (!)LEVEL [This Level # or higher]  or  (!)LEVEL [At least this Level #] [But no more than this Level #]
				if (negReq) {
					if (Array.getLength(splitArgs[1].split(" ")) == 1) { 
						if (thisPlayer.getLevel() < Integer.parseInt(splitArgs[1])) MetReqs++; 
					} else {
						String[] theseLevels = splitArgs[1].split(" ");
						if (thisPlayer.getLevel() < Integer.parseInt(theseLevels[0]) && thisPlayer.getLevel() > Integer.parseInt(theseLevels[1])) MetReqs++;
					}
				} else {
					if (Array.getLength(splitArgs[1].split(" ")) == 1) { 
						if (thisPlayer.getLevel() >= Integer.parseInt(splitArgs[1])) MetReqs++; 
					} else {
						String[] theseLevels = splitArgs[1].split(" ");
						if (thisPlayer.getLevel() >= Integer.parseInt(theseLevels[0]) && thisPlayer.getLevel() <= Integer.parseInt(theseLevels[1])) MetReqs++;
					}
				}

			case NOTABLE: // (!)NOTABLE [Name of Notable]
				if (negReq) if (!GetNotableCompletion(thisPlayer, splitArgs[1])) MetReqs++;
				else if (GetNotableCompletion(thisPlayer, splitArgs[1])) MetReqs++;
				
			case WORLD:  // (!)WORLD [World Name] [or this World Name] [or this World...]
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
				
			case NAME:  // (!)Name [Name] [or this Name] [or this Name, etc...]
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
				
			case STORMY:  // (!)STORMY     - Note that it can still be raining and this will trigger
				if (negReq) if (!thisPlayer.getWorld().isThundering()) MetReqs++;
				else if (thisPlayer.getWorld().isThundering()) MetReqs++;

			case SUNNY:  // (!)SUNNY    - Negative would trigger on Raining or Storming
			    if (negReq) if (thisPlayer.getWorld().hasStorm()) MetReqs++;
				else if (!thisPlayer.getWorld().hasStorm()) MetReqs++;

			case MONEY: // (!)MONEY [Amount of Money, or more]
			    if (negReq) if (!Denizen.econ.has(thisPlayer.toString(), Integer.parseInt(splitArgs[1]))) MetReqs++;
				else if (Denizen.econ.has(thisPlayer.toString(), Integer.parseInt(splitArgs[1]))) MetReqs++;

			case ITEM: // (!)ITEM [ITEM_NAME] [# of that item, or more] [ENCHANTMENT_TYPE]
				String[] theseItemArgs = splitArgs[1].split(" ");
				ItemStack thisItem = new ItemStack(Material.getMaterial(theseItemArgs[0]), Integer.parseInt(theseItemArgs[1]));
				Map<Material, Integer> PlayerInv = new HashMap<Material, Integer>();
				Map<Material, Boolean> isEnchanted = new HashMap<Material, Boolean>();
				
				for (ItemStack invItem : thisPlayer.getInventory()) {
					if (PlayerInv.containsKey(invItem.getType())) { int t = PlayerInv.get(invItem); t = t + invItem.getAmount(); PlayerInv.put(invItem.getType(), t); }
					else PlayerInv.put(invItem.getType(), invItem.getAmount());
					if (!theseItemArgs[2].isEmpty()) if (invItem.containsEnchantment(Enchantment.getByName(theseItemArgs[2]))) isEnchanted.put(invItem.getType(), true);
				}
				
				if (negReq) {
					if (PlayerInv.containsKey(thisItem.getType()) && theseItemArgs[2].isEmpty()) if (PlayerInv.get(thisItem.getType()) < thisItem.getAmount()) MetReqs++;
					else if (PlayerInv.containsKey(thisItem.getType()) && isEnchanted.get(thisItem.getType())) if (PlayerInv.get(thisItem.getType()) < thisItem.getAmount()) MetReqs++;
				}
				else { 
					if (PlayerInv.containsKey(thisItem.getType()) && theseItemArgs[2].isEmpty()) if (PlayerInv.get(thisItem.getType()) >= thisItem.getAmount()) MetReqs++;
					else if (PlayerInv.containsKey(thisItem.getType()) && isEnchanted.get(thisItem.getType())) if (PlayerInv.get(thisItem.getType()) >= thisItem.getAmount()) MetReqs++;
				}
				
			case HOLDING: // (!)HOLDING [ITEM_NAME] [ENCHANTMENT_TYPE]
				String[] holdingItemArgs = splitArgs[1].split(" ");
				if (negReq) if (!thisPlayer.getItemInHand().getType().equals(Material.getMaterial(holdingItemArgs[0]))) {
					if (holdingItemArgs[1] == null) MetReqs++;
					else if (!Enchantment.getByName(holdingItemArgs[1]).equals(thisPlayer.getItemInHand().getType())) MetReqs++;
				} else {
					if (holdingItemArgs[1] == null) MetReqs++;
					else if (Enchantment.getByName(holdingItemArgs[1]).equals(thisPlayer.getItemInHand().getType())) MetReqs++;
				}

			case POTIONEFFECT: // (!)POTIONEFFECT [POTION_EFFECT_TYPE]
				if (negReq) if (!thisPlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) MetReqs++;			
				else if (thisPlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) MetReqs++;
				
			case SCRIPT: // (!)SCRIPT [Script Name] [Number of times completed, or more]
				if (negReq) if (GetScriptCompletes(thisPlayer, splitArgs[1]) > Integer.parseInt(splitArgs[2])) MetReqs++;
				else if (GetScriptCompletes(thisPlayer, splitArgs[1]) <= Integer.parseInt(splitArgs[2])) MetReqs++;

			case GROUP:
				if (negReq) if (!Denizen.perms.playerInGroup(thisPlayer.getWorld(), thisPlayer.toString(), splitArgs[1])) MetReqs++;
				else if (Denizen.perms.playerInGroup(thisPlayer.getWorld(), thisPlayer.toString(), splitArgs[1])) MetReqs++;		
			}
		}
		if (RequirementsMode.equalsIgnoreCase("all") && MetReqs == RequirementsList.size()) return true;
		String[] ModeArgs = RequirementsMode.split(" ");
		if (ModeArgs[0].equalsIgnoreCase("any") && MetReqs >= Integer.parseInt(ModeArgs[1])) return true;
		
		return false;
	}


}