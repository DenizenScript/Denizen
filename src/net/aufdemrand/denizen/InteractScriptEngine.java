package net.aufdemrand.denizen;

import java.lang.reflect.Array;
import java.util.*;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.*;
import net.citizensnpcs.trait.LookClose;

//import com.gmail.nossr50.datatypes.SkillType;
//import com.gmail.nossr50.api.ExperienceAPI;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.DenizenListener;
import net.aufdemrand.denizen.DenizenCharacter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;


public class InteractScriptEngine {

	private static Denizen plugin;

	public static void initialize() { plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen"); }

	public enum RequirementMode {
		NONE, ALL, ANY
	}

	public enum Requirement {
		NONE, QUEST, NAME, WEARING, INVINSIBLE, ITEM, HOLDING, TIME, PRECIPITATION, ACTIVITY, FINISHED,
		STORMY, SUNNY, HUNGER, WORLD, PERMISSION, LEVEL, SCRIPT, NOTABLE, GROUP, MONEY, POTIONEFFECT, MCMMO }

	public enum Trigger {
		CHAT, CLICK, FINISH, START, TOUCH
	}

	public enum Command {
		DELAY, ZAP, ASSIGN, UNASSIGN, C2SCRIPT, SPAWN, CHANGE, WEATHER, EFFECT, GIVE, TAKE, HEAL, DAMAGE,
		POTION_EFFECT, TELEPORT, STRIKE, WALK, NOD, REMEMBER, BOUNCE, RESPAWN, PERMISS, EXECUTE, SHOUT,
		WHISPER, NARRARATE, CHAT, ANNOUNCE, GRANT, HINT, RETURN, ENGAGE, LOOK, WALKTO
	}



	/* GetDenizensWithinRange
	 *
	 * Requires Player Location, Player World, Range in blocks.
	 * Compiles a list of NPCs with a character type of Denizen
	 * within range of the Player.
	 *
	 * Returns DenizensWithinRange List<NPC>
	 */

	public static boolean CheckRequirements(String thisScript, Player thisPlayer) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

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
					{ MetReqs++;
					break; }
					else if (splitArgs[1].equalsIgnoreCase("NIGHT")) if (thisPlayer.getWorld().getTime() < 16000)
					{ MetReqs++; break; }
					else {
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
				
				
			case PERMISSION:  // (-)PERMISSION [this.permission.node]
				if (negReq) if (!Denizen.perms.playerHas(thisPlayer.getWorld(), thisPlayer.getName(),
						splitArgs[1])) MetReqs++;
				else if (Denizen.perms.playerHas(thisPlayer.getWorld(), thisPlayer.getName(),
						splitArgs[1])) MetReqs++;
				break;

			case MCMMO:  // (-)MCMMO STAT LEVEL

				//		ExperienceAPI mcMMOAPI = null;
				//	thisPlayer.sendMessage("Your power level is " + mcMMOAPI.getPowerLevel(thisPlayer));
				//break;

			case PRECIPITATION:  // (-)PRECIPITATION
				if (negReq) if (!thisPlayer.getWorld().hasStorm()) MetReqs++;
				else if (thisPlayer.getWorld().hasStorm()) MetReqs++;
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

			case NOTABLE: // (-)NOTABLE [Name of Notable]
				if (negReq) if (!GetNotableCompletion(thisPlayer, splitArgs[1])) MetReqs++;
				else if (GetNotableCompletion(thisPlayer, splitArgs[1])) MetReqs++;
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

			case STORMY:  // (-)STORMY     - Note that it can still be raining and this will trigger
				if (negReq) if (!thisPlayer.getWorld().isThundering()) MetReqs++;
				else if (thisPlayer.getWorld().isThundering()) MetReqs++;
				break;

			case SUNNY:  // (-)SUNNY    - Negative would trigger on Raining or Storming
				if (negReq) if (thisPlayer.getWorld().hasStorm()) MetReqs++;
				else if (!thisPlayer.getWorld().hasStorm()) MetReqs++;
				break;

			case MONEY: // (-)MONEY [Amount of Money, or more]
				if (negReq) if (!Denizen.econ.has(thisPlayer.getName(), Integer.parseInt(splitArgs[1]))) MetReqs++;
				else if (Denizen.econ.has(thisPlayer.getName(), Integer.parseInt(splitArgs[1]))) MetReqs++;
				break;

			case ITEM: // (-)ITEM [ITEM_NAME] (# of that item, or more) (ENCHANTMENT_TYPE)
				String[] theseItemArgs = splitArgs[1].split(" ");
				
				int itemAmt = 1;
				
				if (theseItemArgs[1] != null) itemAmt = Integer.parseInt(theseItemArgs[1]);
				
				ItemStack thisItem = new ItemStack(Material.getMaterial(theseItemArgs[0]), itemAmt);
				Map<Material, Integer> PlayerInv = new HashMap<Material, Integer>();
				Map<Material, Boolean> isEnchanted = new HashMap<Material, Boolean>();

				for (ItemStack invItem : thisPlayer.getInventory()) {
					if (PlayerInv.containsKey(invItem.getType())) {
						int t = PlayerInv.get(invItem.getType());
						t = t + invItem.getAmount(); PlayerInv.put(invItem.getType(), t);
					}
					else PlayerInv.put(invItem.getType(), invItem.getAmount());
					if (theseItemArgs[2] != null)
						if (invItem.containsEnchantment(Enchantment.getByName(theseItemArgs[2])))
							isEnchanted.put(invItem.getType(), true);
				}

				if (negReq) {
					if (PlayerInv.containsKey(thisItem.getType()) && theseItemArgs[2] == null)
						if (PlayerInv.get(thisItem.getType()) < thisItem.getAmount()) MetReqs++;
						else if (PlayerInv.containsKey(thisItem.getType()) && isEnchanted.get(thisItem.getType()))
							if (PlayerInv.get(thisItem.getType()) < thisItem.getAmount()) MetReqs++;
				}
				else {
					if (PlayerInv.containsKey(thisItem.getType()) && theseItemArgs[2] == null)
						if (PlayerInv.get(thisItem.getType()) >= thisItem.getAmount()) MetReqs++;
						else if (PlayerInv.containsKey(thisItem.getType()) && isEnchanted.get(thisItem.getType()))
							if (PlayerInv.get(thisItem.getType()) >= thisItem.getAmount()) MetReqs++;
				}
				break;

			case HOLDING: // (-)HOLDING [ITEM_NAME] (ENCHANTMENT_TYPE)
				String[] itemArgs = splitArgs[1].split(" ");
				if (negReq) if (thisPlayer.getItemInHand().getType() != Material.getMaterial(itemArgs[0])) {
					if (itemArgs[1] == null) MetReqs++;
					else if (!thisPlayer.getItemInHand().getEnchantments().containsKey(Enchantment.getByName(itemArgs[1])))
						MetReqs++;
				} else if (thisPlayer.getItemInHand().getType() == Material.getMaterial(itemArgs[0])) {
					if (itemArgs[1] == null) MetReqs++;
					else if (thisPlayer.getItemInHand().getEnchantments().containsKey(Enchantment.getByName(itemArgs[1])))
						MetReqs++;
				}
				break;

			case POTIONEFFECT: // (-)POTIONEFFECT [POTION_EFFECT_TYPE]
				if (negReq) if (!thisPlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) MetReqs++;
				else if (thisPlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) MetReqs++;
				break;

			case FINISHED:
			case SCRIPT: // (-)SCRIPT [Script Name]
				if (negReq) if (!GetScriptComplete(thisPlayer, splitArgs[1])) MetReqs++;
				else if (GetScriptComplete(thisPlayer, splitArgs[1])) MetReqs++;
				break;

			case GROUP:
				if (negReq) if (!Denizen.perms.playerInGroup(thisPlayer.getWorld(), thisPlayer.getName(),
						splitArgs[1])) MetReqs++;
				else if (Denizen.perms.playerInGroup(thisPlayer.getWorld(), thisPlayer.getName(),
						splitArgs[1])) MetReqs++;
				break;
			}
		}

		//		plugin.getServer().broadcastMessage("Met requirements for this script: " + MetReqs);

		if (RequirementsMode.equalsIgnoreCase("all") && MetReqs == RequirementsList.size()) return true;
		String[] ModeArgs = RequirementsMode.split(" ");
		if (ModeArgs[0].equalsIgnoreCase("any") && MetReqs >= Integer.parseInt(ModeArgs[1])) return true;

		return false;
	}



	public static List<NPC> GetDenizensWithinRange (Location PlayerLocation, World PlayerWorld, int Range) {

		List<NPC> DenizensWithinRange = new ArrayList<NPC>();
		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCManager().
				getNPCs(DenizenCharacter.class);
		if (DenizenNPCs.isEmpty()) return DenizensWithinRange;
		List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (NPC aDenizenList : DenizenList) {
			if (aDenizenList.isSpawned())	{
				if (aDenizenList.getBukkitEntity().getWorld().equals(PlayerWorld)) {
					if (aDenizenList.getBukkitEntity().getLocation().distance(PlayerLocation) < Range)
						DenizensWithinRange.add(aDenizenList);
				}
			}

		}
		return DenizensWithinRange;
	}



	/* GetPlayersWithinRange
	 *
	 * Requires Player Location, Player World, Range in blocks.
	 * Compiles a list of NPCs with a character type of Denizen
	 * within range of the Player.
	 *
	 * Returns PlayersWithinRange List<Player>
	 */

	public static List<Player> GetPlayersWithinRange (Location DenizenLocation, World DenizenWorld, int Range) {

		List<Player> PlayersWithinRange = new ArrayList<Player>();
		Player[] DenizenPlayers = plugin.getServer().getOnlinePlayers();
		for (Player aPlayer : DenizenPlayers) {
			if (aPlayer.isOnline())	{
				if (aPlayer.getWorld().equals(DenizenWorld)) {
					if (aPlayer.getLocation().distance(DenizenLocation) < Range)
						PlayersWithinRange.add(aPlayer);
				}
			}

		}
		return PlayersWithinRange;

	}











	/* TalkToNPC
	 *
	 * Requires the NPC Denizen, Player, and the Message to relay.
	 * Sends the message from Player to Denizen with the formatting
	 * as specified in the config.yml talk_to_npc_string.
	 *
	 * <NPC> and <TEXT> are replaced with corresponding information.
	 */





	/* GetInteractScript
	 *
	 * Requires the Denizen and the Player
	 * Checks the Denizens scripts and returns the script that meets requirements and has
	 * the highest weight.  If no script matches, returns "none".
	 *
	 * Returns theScript
	 * Calls CheckRequirements
	 */

	public static String GetInteractScript(NPC thisDenizen, Player thisPlayer) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		String theScript = "none";
		List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getName()
				+ ".Interact Scripts");
		if (ScriptList.isEmpty()) { return theScript; }
		List<String> ScriptsThatMeetRequirements = new ArrayList<String>();
		// Get scripts that meet requirements
		for (String thisScript : ScriptList) {
			String [] thisScriptArray = thisScript.split(" ", 2);
			if (CheckRequirements(thisScriptArray[1], thisPlayer))
				ScriptsThatMeetRequirements.add(thisScript);

		}
		// Get highest scoring script
		if (ScriptsThatMeetRequirements.size() > 1) {

			int ScriptPriority = -1;

			for (String thisScript : ScriptsThatMeetRequirements) {
				String [] thisScriptArray = thisScript.split(" ", 2);
				if (Integer.parseInt(thisScriptArray[0]) > ScriptPriority) {ScriptPriority =
						Integer.parseInt(thisScriptArray[0]); theScript = thisScript; }
			}
		}
		else if (ScriptsThatMeetRequirements.size() == 1) theScript = ScriptsThatMeetRequirements.get(0);

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






	/* TriggerToQue
	 *
	 * Requires the Script, the Current Step, the Chat Trigger to trigger, and the Player
	 * Triggers the script for the chat trigger of the step and script specified.
	 *
	 * Calls ScriptHandler to handle the commands in the script. ScriptHandler returns any
	 * raw text that needs to be sent to the player which is put in the PlayerQue for
	 * output.
	 */











	/* GetCurrentStep
	 *
	 * Requires the Player and the Script.
	 * Reads the config.yml to find the current step that the player is on
	 * for the specified script.
	 *
	 * Returns currentStep
	 */

	public static int GetCurrentStep(Player thePlayer, String theScript) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		int currentStep = 1;
		if (plugin.getConfig().getString("Players." + thePlayer.getDisplayName() + "." + theScript + "." + "Current Step")
				!= null) currentStep =  plugin.getConfig().getInt("Players." + thePlayer.getDisplayName() + "." + theScript
						+ "." + "Current Step");
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

	public static boolean GetScriptComplete(Player thePlayer, String theScript) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		boolean ScriptComplete = false;
		if (plugin.getConfig().getString("Players." + thePlayer + "." + theScript + "." + "Completed")
				!= null) ScriptComplete = true;
		return ScriptComplete;
	}


	/* GetNotableCompletion
	 *
	 * Requires the Player and the Script.
	 * Reads the config.yml to find if the player has completed
	 * the specified script.
	 *
	 * Returns number of times script has been completed.
	 */




	/* GetChatTriggers
	 *
	 * Requires the Script and the Current Step.
	 * Gets a list of Chat Triggers for the step of the script specified.
	 * Chat Triggers are words required to trigger one of the chat 
	 *
	 * Returns ChatTriggers
	 */

	public static List<String> GetChatTriggers(String theScript, Integer currentStep) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		List<String> ChatTriggers = new ArrayList<String>();
		int currentTrigger = 1;
		for (int x=1; currentTrigger >= 0; x++) {
			String theChatTrigger = plugin.getScripts().getString("" + theScript + ".Steps."
					+ currentStep + ".Chat Trigger." + String.valueOf(currentTrigger) + ".Trigger");
			if (theChatTrigger != null) { 
				ChatTriggers.add(theChatTrigger.split("/")[1]); 
				currentTrigger = x + 1; 
			}
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

	public static String GetScriptName(String thisScript) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		if (thisScript.equals("none")) { return thisScript; }
		else {
			String [] thisScriptArray = thisScript.split(" ", 2);
			return thisScriptArray[1]; }
	}



	// CHECK REQUIREMENTS  (Checks if the requirements of a script are met when given Script/Player)

	/* TalkToNPC
	 *
	 * Requires the NPC Denizen, Player, and the Message to relay.
	 * Sends the message from Player to Denizen with the formatting
	 * as specified in the config.yml talk_to_npc_string.
	 *
	 * <NPC> and <TEXT> are replaced with corresponding information.
	 */

	public static void TalkToNPC(NPC theDenizen, Player thePlayer, String theMessage) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		thePlayer.sendRawMessage(plugin.getConfig().getString("chat_to_npc_string", "You say to <NPC>, <TEXT>").replace("<NPC>", theDenizen.getName()).
				replace("<TEXT>", theMessage));
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


	public static void ParseScript(NPC theDenizen, Player thePlayer, String theScript,


			String theMessage,  Trigger theTrigger) {


		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		int CurrentStep = GetCurrentStep(thePlayer, theScript);

		switch (theTrigger) {

		case CHAT:
			List<String> ChatTriggerList = GetChatTriggers(theScript, CurrentStep);
			for (int l=0; l < ChatTriggerList.size(); l++ ) {
				if (theMessage.toLowerCase().contains(ChatTriggerList.get(l).replace("<PLAYER>", thePlayer.getName()).toLowerCase())) {

					TalkToNPC(theDenizen, thePlayer, plugin.getScripts().getString("" + theScript + ".Steps."
							+ CurrentStep + ".Chat Trigger." + String.valueOf(l + 1) + ".Trigger").replace("/", ""));

					TriggerToQue(theScript, plugin.getScripts().getStringList("" + theScript + ".Steps."
							+ CurrentStep + ".Chat Trigger." + String.valueOf(l + 1) + ".Script"), CurrentStep, thePlayer, theDenizen);
					return;
				}

			}

			TalkToNPC(theDenizen, thePlayer, theMessage);

			List<String> CurrentPlayerQue = new ArrayList<String>();
			if (Denizen.playerQue.get(thePlayer) != null) CurrentPlayerQue = Denizen.playerQue.get(thePlayer);
			Denizen.playerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add

			CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";"
					+ 0 + ";CHAT;" + "CHAT " + plugin.getConfig().getString("Denizens." + theDenizen.getId() 
							+ ".Texts.No Script Interact", "I have nothing to say to you at this time."));

			Denizen.playerQue.put(thePlayer, CurrentPlayerQue);


			return;

		case CLICK:

			TriggerToQue(theScript, plugin.getScripts().getStringList("" + theScript + ".Steps."
					+ CurrentStep + ".Click Trigger.Script"), CurrentStep, thePlayer, theDenizen);

			return;

		case FINISH:
			// get current progressions
			// send script

		}
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






	/* TriggerToQue
	 *
	 * Requires the Script, the Current Step, the Chat Trigger to trigger, and the Player
	 * Triggers the script for the chat trigger of the step and script specified.
	 *
	 * Calls ScriptHandler to handle the commands in the script. ScriptHandler returns any
	 * raw text that needs to be sent to the player which is put in the PlayerQue for
	 * output.
	 */

	public static void TriggerToQue(String theScript, List<String> AddedToPlayerQue, int CurrentStep, Player thePlayer,
			NPC theDenizen) {


		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		List<String> CurrentPlayerQue = new ArrayList<String>();
		if (Denizen.playerQue.get(thePlayer) != null) CurrentPlayerQue = Denizen.playerQue.get(thePlayer);
		Denizen.playerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add

		if (!AddedToPlayerQue.isEmpty()) {

			for (String theCommand : AddedToPlayerQue) {


				// Longer than 40, probably a long chat that needs multiline formatting.
				if (theCommand.length() > 40) {

					String[] theCommandText;
					theCommandText = theCommand.split(" ");

					switch (Command.valueOf(theCommandText[0].toUpperCase())) {
					case SHOUT:
					case CHAT:
					case WHISPER:
					case ANNOUNCE:
					case NARRARATE:
						int word = 1;
						int line = 0;
						ArrayList<String> multiLineCommand = new ArrayList<String>();
						multiLineCommand.add(theCommandText[0]);
						while (word < theCommandText.length) {
							if (line==0) {
								if (multiLineCommand.get(line).length() + theCommandText[word].length() + theDenizen.getName().length() < 50) {
									multiLineCommand.set(line, multiLineCommand.get(line) + " " + theCommandText[word]);
									word++;
								}
								else {
									line++; multiLineCommand.add(theCommandText[0] + " *");
								}
							}
							else {
								if (multiLineCommand.get(line).length() + theCommandText[word].length() < 60) {
									multiLineCommand.set(line, multiLineCommand.get(line) + " " + theCommandText[word]);
									word++;
								}
								else {
									line++; multiLineCommand.add(theCommandText[0] + " *");
								}
							}
						}
						for (String eachCommand : multiLineCommand) {
							CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";"
									+ Integer.toString(CurrentStep) + ";CHAT;" + eachCommand);
						}
					}
				}
				else CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";"
						+ Integer.toString(CurrentStep) + ";CHAT;" + theCommand);
			}
			Denizen.playerQue.put(thePlayer, CurrentPlayerQue);
		}
	}



	public static void CommandExecuter(Player thePlayer, String theStep) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		// Syntax of theStep
		// 0 Denizen ID; 1 Script Name; 2 Step Number; 3 Trigger Type; 4 Command

		String[] splitArgs = theStep.split(";");
		String[] splitCommand = splitArgs[4].split(" ");

		if (splitCommand[0].startsWith("^")) splitCommand[0] = splitCommand[0].substring(1);

		switch (Command.valueOf(splitCommand[0].toUpperCase())) {

		// SCRIPT INTERACTION

		case ZAP:  // ZAP [Optional Step # to advance to]

			if (splitCommand.length == 1) { plugin.getConfig().set("Players." + thePlayer.getDisplayName()
					+ "." + splitArgs[1] + ".Current Step", Integer.parseInt(splitArgs[2]) + 1);
			plugin.saveConfig();}
			else { plugin.getConfig().set("Players." + thePlayer.getDisplayName() + "." + splitArgs[1]
					+ ".Current Step", Integer.parseInt(splitCommand[1])); plugin.saveConfig(); }
			break;

		case ASSIGN:  // ASSIGN [ME|Denizen Name] [ALL|Player Name] [Priority] [Script Name]
		case UNASSIGN:  // DEASSIGN [ME|Denizen Name] [ALL|Player Name] [Script Name]
		case C2SCRIPT:  // Runs a CitizenScript

			// WORLD INTERACTION

		case SPAWN:  // SPAWN [MOB NAME] [AMOUNT] (Location Bookmark)
		case CHANGE:  // CHANGE [Block State Bookmark]
		case WEATHER:  // WEATHER [Sunny|Stormy|Rainy] (Duration for Stormy/Rainy)

			if (splitCommand[1].equalsIgnoreCase("sunny")) { thePlayer.getWorld().setStorm(false); }
			else if (splitCommand[1].equalsIgnoreCase("stormy")) { thePlayer.getWorld().setThundering(true); }
			else if (splitCommand[1].equalsIgnoreCase("rainy")) { thePlayer.getWorld().setStorm(true); }
			break;

		case EFFECT:  // EFFECT [EFFECT_TYPE] (Location Bookmark)

			// PLAYER INTERACTION

		case LOOK: // ENG

			if (splitCommand[1].equalsIgnoreCase("CLOSE")) {
				if (!CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0])).getTrait(LookClose.class).toggle())
					CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0])).getTrait(LookClose.class).toggle();
			}
			if (splitCommand[1].equalsIgnoreCase("AWAY")) {
				if (CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0])).getTrait(LookClose.class).toggle())
					CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0])).getTrait(LookClose.class).toggle();
			}
			break;

		case GIVE:  // GIVE [Item:Data] [Amount] [ENCHANTMENT_TYPE]

			ItemStack giveItem = new ItemStack(Material.getMaterial(splitCommand[1].toUpperCase()));
			giveItem.setAmount(Integer.valueOf(splitCommand[2]));

			CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0])).getBukkitEntity().getWorld()
			.dropItem(CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0])).getBukkitEntity().getLocation().add(
					CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0])).getBukkitEntity().getLocation().getDirection().multiply(1.1)), giveItem);


			break;
		case TAKE:  // TAKE [Item] [Amount]   or  TAKE ITEM_IN_HAND  or  TAKE MONEY [Amount]
			// or  TAKE ENCHANTMENT  or  TAKE INVENTORY
		case HEAL:  // HEAL  or  HEAL [# of Hearts]
		case DAMAGE:
		case POTION_EFFECT:
		case TELEPORT:  // TELEPORT [Location Notable] (Effect)
			// or TELEPORT [X,Y,Z] (World Name) (Effect)
		case STRIKE:  // STRIKE    Strikes lightning on the player, with damage.

			thePlayer.getWorld().strikeLightning(thePlayer.getLocation());
			break;

			// DENIZEN INTERACTION

		case WALK:  // WALK Z(-NORTH(2)/+SOUTH(0)) X(-WEST(1)/+EAST(3)) Y (+UP/-DOWN)

			NPC theDenizenToWalk = CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0]));

			Denizen.previousDenizenLocation.put(theDenizenToWalk, theDenizenToWalk.getBukkitEntity().getLocation());
			if (!splitCommand[1].isEmpty()) theDenizenToWalk.getAI().setDestination(theDenizenToWalk.getBukkitEntity().getLocation()
					.add(Double.parseDouble(splitCommand[2]), Double.parseDouble(splitCommand[3]), Double.parseDouble(splitCommand[1])));
			break;

		case WALKTO:  // WALKTO [Location Bookmark]

			NPC theDenizenToWalkTo = CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0]));

			Denizen.previousDenizenLocation.put(theDenizenToWalkTo, theDenizenToWalkTo.getBukkitEntity().getLocation());
			if (!splitCommand[1].isEmpty()) {

				List<String> locationList = plugin.getConfig().getStringList("Denizens." + theDenizenToWalkTo.getName() + ".Bookmarks.Location");

				String[] theLocation = null;

				for (String thisLocation : locationList) {
					String theName = thisLocation.split(" ", 2)[0];
					if (theName.equalsIgnoreCase(splitCommand[1])) theLocation = thisLocation.split(" ", 2)[1].split(";");
				}
				
				if (theLocation != null) {			

				//	plugin.getServer().broadcastMessage(theLocation[0] + theLocation[1]);
					Location locationBookmark = 
							new Location(plugin.getServer().getWorld(theLocation[0]),
									Double.parseDouble(theLocation[1]), Double.parseDouble(theLocation[2]),
									Double.parseDouble(theLocation[3]));

					theDenizenToWalkTo.getAI().setDestination(locationBookmark);
				}
				
				plugin.getServer().broadcastMessage("Has destination? " + theDenizenToWalkTo.getAI().hasDestination());
				
			}
			break;


		case RETURN:

			NPC theDenizenToReturn = CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0]));
			if (Denizen.previousDenizenLocation.containsKey(theDenizenToReturn))
				theDenizenToReturn.getAI().setDestination(Denizen.previousDenizenLocation.
						get(theDenizenToReturn));
			break;

		case NOD:  // NOD

			break;

		case REMEMBER:  // REMEMBER [CHAT|LOCATION|INVENTORY]


		case RESPAWN:  // RESPAWN [ME|Denizen Name] [Location Notable]

			NPC theDenizenSpawning = CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0]));
			break;

		case PERMISS:  // PERMISS [Permission Node]

			Denizen.perms.playerAdd(thePlayer, splitCommand[1]);
			break;


		case EXECUTE:  // EXECUTE [Command to Execute]

			thePlayer.getServer().dispatchCommand(null, splitArgs[4].split(" ", 2)[1].replace("<PLAYER>", thePlayer.getName().replace("<WORLD>", thePlayer.getWorld().getName())));
			break;

			// SHOUT can be heard by players within 100 blocks.
			// WHISPER can only be heard by the player interacting with.
			// CHAT can be heard by the player, and players within 5 blocks.
			// NARRARATE can only be heard by the player and is not branded by the NPC.
			// ANNOUNCE can be heard by the entire server.

		case WHISPER:  // ZAP [Optional Step # to advance to]
		case NARRARATE:  // ZAP [Optional Step # to advance to]
		case SHOUT:  // ZAP [Optional Step # to advance to]
		case CHAT:  // CHAT [Message]

			NPC theDenizenChatting = CitizensAPI.getNPCManager().getNPC(Integer.valueOf(splitArgs[0]));

			if (splitArgs[4].split(" ", 2)[1].startsWith("*"))
				thePlayer.sendMessage("    " + splitArgs[4].split(" ", 2)[1].replace("*", ""));
			else thePlayer.sendMessage(plugin.getConfig().getString("npc_chat_to_player").replace("<TEXT>", splitArgs[4].split(" ", 2)[1]).replace("<NPC>", CitizensAPI.getNPCManager().getNPC(Integer.parseInt(splitArgs[0])).getName()));

			for (Player eachPlayer : GetPlayersWithinRange(theDenizenChatting.getBukkitEntity().getLocation(), 
					theDenizenChatting.getBukkitEntity().getWorld(),
					plugin.getConfig().getInt("npc_to_player_chat_range_in_blocks", 15))) {

				if (eachPlayer != thePlayer) {

					if (splitArgs[4].split(" ", 2)[1].startsWith("*"))
						eachPlayer.sendMessage("    " + splitArgs[4].split(" ", 2)[1].replace("*", ""));
					else eachPlayer.sendMessage(plugin.getConfig().getString("npc_chat_to_player_bystander").replace("<TEXT>", splitArgs[4].split(" ", 2)[1]).replace("<PLAYER>", thePlayer.getDisplayName()).replace("<NPC>", CitizensAPI.getNPCManager().getNPC(Integer.parseInt(splitArgs[0])).getName()));
				}}

			break;

		case ANNOUNCE: // ANNOUNCE [Message]

			// NOTABLES

		case GRANT:  // ACHIEVE [Name of Achievement Notable to Grant]

		}
	}







	/* GetNotableCompletion
	 *
	 * Requires the Player and the Script.
	 * Reads the config.yml to find if the player has completed
	 * the specified script.
	 *
	 * Returns number of times script has been completed.
	 */

	public static boolean GetNotableCompletion(Player thePlayer, String theNotable) {
		return plugin.getConfig().getStringList("Notables.Players." + thePlayer + "." + theNotable).
				contains(theNotable);
	}


}