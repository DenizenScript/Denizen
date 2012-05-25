package net.aufdemrand.denizen;

import java.lang.reflect.Array;
import java.util.*;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;

//import com.gmail.nossr50.datatypes.SkillType;
//import com.gmail.nossr50.api.ExperienceAPI;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.DenizenCharacter;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class InteractScriptEngine {

	private static Denizen plugin;

	public static void initialize() { plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen"); }

	public enum RequirementMode {
		NONE, ALL, ANY
	}

	public enum Requirement {
		NONE, QUEST, NAME, WEARING, INVINSIBLE, ITEM, HOLDING, TIME, PRECIPITATION, ACTIVITY, FINISHED,
		STORMY, SUNNY, HUNGER, WORLD, PERMISSION, LEVEL, SCRIPT, NOTABLE, GROUP, MONEY, POTIONEFFECT, MCMMO, PRECIPITATING, STORMING }

	public enum Trigger {
		CHAT, CLICK, FINISH, PROXIMITY
	}

	public enum Command {
		WAIT, ZAP, ASSIGN, UNASSIGN, C2SCRIPT, SPAWN, CHANGE, WEATHER, EFFECT, GIVE, TAKE, HEAL, DAMAGE,
		POTION_EFFECT, TELEPORT, STRIKE, WALK, NOD, REMEMBER, BOUNCE, RESPAWN, PERMISS, EXECUTE, SHOUT,
		WHISPER, CHAT, ANNOUNCE, GRANT, HINT, RETURN, ENGAGE, LOOK, WALKTO, FINISH, FOLLOW, CAST, NARRATE, SWITCH, PRESS
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



			case MCMMO:  // (-)MCMMO STAT LEVEL

				//		ExperienceAPI mcMMOAPI = null;
				//	thisPlayer.sendMessage("Your power level is " + mcMMOAPI.getPowerLevel(thisPlayer));
				//break;

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

			case NOTABLE: // (-)NOTABLE [Name of Notable]

				//if (negReq) if (!GetNotableCompletion(thisPlayer, splitArgs[1])) MetReqs++;
				//else if (GetNotableCompletion(thisPlayer, splitArgs[1])) MetReqs++;
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
				if (negReq) { if (!Denizen.econ.has(thisPlayer.getName(), Integer.parseInt(splitArgs[1]))){ MetReqs++;} }
				else if (Denizen.econ.has(thisPlayer.getName(), Integer.parseInt(splitArgs[1]))) {MetReqs++;}
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
				if (negReq) { if (!GetScriptComplete(thisPlayer, splitArgs[1])) MetReqs++; }
				else if (GetScriptComplete(thisPlayer, splitArgs[1])) MetReqs++;
				break;

			case GROUP:
				if (negReq) { if (!Denizen.perms.playerInGroup(thisPlayer.getWorld(), thisPlayer.getName(),
						splitArgs[1])) MetReqs++; }
				else if (Denizen.perms.playerInGroup(thisPlayer.getWorld(), thisPlayer.getName(),
						splitArgs[1])) MetReqs++;
				break;

			case PERMISSION:  // (-)PERMISSION [this.permission.node]
				if (negReq) { if (!Denizen.perms.playerHas(thisPlayer.getWorld(), thisPlayer.getName(),
						splitArgs[1])) MetReqs++; }
				else if (Denizen.perms.playerHas(thisPlayer.getWorld(), thisPlayer.getName(),
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
		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().
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
		if (plugin.getConfig().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Current Step")
				!= null) currentStep =  plugin.getConfig().getInt("Players." + thePlayer.getName() + "." + theScript
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
		if (plugin.getConfig().getString("Players." + thePlayer.getName() + "." + theScript + "." + "Completed")
				!= null) ScriptComplete = true;
		return ScriptComplete;
	}




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

		thePlayer.sendRawMessage(plugin.getConfig().getString("player_chat_to_npc", "You say to <NPC>, <TEXT>").replace("<NPC>", theDenizen.getName()).
				replace("<TEXT>", theMessage).replace("<PLAYER>", thePlayer.getName()));
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


	public static void ParseScript(NPC theDenizen, Player thePlayer, String theScript, String theMessage,  Trigger theTrigger) {

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
					+ 0 + ";" + String.valueOf(System.currentTimeMillis()) + ";" + "CHAT " + plugin.getConfig().getString("Denizens." + theDenizen.getId() 
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

				String[] theCommandText;
				theCommandText = theCommand.split(" ");

				// Longer than 40, probably a long chat that needs multiline formatting.
				if (theCommand.length() > 40) {

					switch (Command.valueOf(theCommandText[0].toUpperCase())) {
					case SHOUT:	case CHAT: case WHISPER: case ANNOUNCE:	case NARRATE:
						int word = 1; int line = 0;
						ArrayList<String> multiLineCommand = new ArrayList<String>();
						multiLineCommand.add(theCommandText[0]);
						while (word < theCommandText.length) {
							if (line==0) {
								if (multiLineCommand.get(line).length() + theCommandText[word].length() + theDenizen.getName().length() < 48) {
									multiLineCommand.set(line, multiLineCommand.get(line) + " " + theCommandText[word]);
									word++;
								}
								else { line++; multiLineCommand.add(theCommandText[0] + " *"); }
							}
							else {
								if (multiLineCommand.get(line).length() + theCommandText[word].length() < 58) {
									multiLineCommand.set(line, multiLineCommand.get(line) + " " + theCommandText[word]);
									word++;
								}
								else { line++; multiLineCommand.add(theCommandText[0] + " *"); }
							}
						}

						for (String eachCommand : multiLineCommand) {
							CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";" + Integer.toString(CurrentStep) + ";" + String.valueOf(System.currentTimeMillis()) + ";" + eachCommand);
						}
					}
				}
				else if (theCommandText[0].equalsIgnoreCase("WAIT")) {
					Long timeDelay = Long.parseLong(theCommandText[1]) * 1000;
					String timeWithDelay = String.valueOf(System.currentTimeMillis() + timeDelay);
					CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";" + Integer.toString(CurrentStep) + ";" + timeWithDelay + ";" + theCommand);						
				}
				else {
					CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";" + Integer.toString(CurrentStep) + ";" + String.valueOf(System.currentTimeMillis()) + ";" + theCommand);	
				}
			}
			Denizen.playerQue.put(thePlayer, CurrentPlayerQue);
		}
	}



	public static void CommandExecuter(Player thePlayer, String theStep) {

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

		// Syntax of theStep
		// 0 Denizen ID; 1 Script Name; 2 Step Number; 3 Trigger Type; 4 Command

		String[] rawqueArgs = theStep.split(";");
		String[] commandArgs = rawqueArgs[4].split(" ");

		if (commandArgs[0].startsWith("^")) commandArgs[0] = commandArgs[0].substring(1);

		switch (Command.valueOf(commandArgs[0].toUpperCase())) {

		// SCRIPT INTERACTION

		case ZAP:  // ZAP [Optional Step # to advance to]

			if (commandArgs.length == 1) { plugin.getConfig().set("Players." + thePlayer.getName()
					+ "." + rawqueArgs[1] + ".Current Step", Integer.parseInt(rawqueArgs[2]) + 1);
			plugin.saveConfig();}
			else { plugin.getConfig().set("Players." + thePlayer.getName() + "." + rawqueArgs[1]
					+ ".Current Step", Integer.parseInt(commandArgs[1])); plugin.saveConfig(); }
			break;

		case ASSIGN:  // ASSIGN [ME|Denizen Name] [ALL|Player Name] [Priority] [Script Name]
		case UNASSIGN:  // DEASSIGN [ME|Denizen Name] [ALL|Player Name] [Script Name]
		case C2SCRIPT:  // Runs a CitizenScript

			// WORLD INTERACTION

		case SPAWN:  // SPAWN [MOB NAME] [AMOUNT] (Location Bookmark)

			Location theSpawnLoc = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])).getBukkitEntity().getLocation();
			if (commandArgs.length > 3) theSpawnLoc = getLocationBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])), commandArgs[3], "Location");
			if (theSpawnLoc != null) {
				for (int cx = 1; cx < Integer.valueOf("commandArgs[2]"); cx++) {
					thePlayer.getWorld().spawnCreature(theSpawnLoc, EntityType.valueOf(commandArgs[1]));	
				}
			}
			break;

		case SWITCH:  // SWITCH [Block Bookmark] ON|OFF

			Boolean switchState = false;
			if (commandArgs[2].equalsIgnoreCase("ON")) switchState = true;
			Location switchLoc = getLocationBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])), commandArgs[1], "Block");
			if (switchLoc.getBlock().getType() == Material.LEVER) {
//				if (switchLoc.getBlock().getData() <= ((byte) 8) && switchState) {
	//				switchLoc.getBlock().setData((byte) (switchLoc.getBlock().getData() + ((byte)8)), true);
		//			switchLoc.getBlock().getState().update();
			//	}
				//if (switchLoc.getBlock().getData() >= ((byte) 8) && !switchState) {
				//	switchLoc.getBlock().setData((byte) (switchLoc.getBlock().getData() - ((byte)8)), true);
				//	switchLoc.getBlock().getState().update();
					
					World theWorld = switchLoc.getWorld();
					net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), switchLoc.getBlockX(), switchLoc.getBlockY(), switchLoc.getBlockZ(), null);
			//	}
			}
			break;

		case PRESS:  // SWITCH [Block Bookmark] ON|OFF

			Location pressLoc = getLocationBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])), commandArgs[1], "Block");
			if (pressLoc.getBlock().getType() == Material.STONE_BUTTON) {
			
				World theWorld = pressLoc.getWorld();
				net.minecraft.server.Block.STONE_BUTTON.interact(((CraftWorld)theWorld).getHandle(), pressLoc.getBlockX(), pressLoc.getBlockY(), pressLoc.getBlockZ(), null);
		
				
			}
			
			break;

			
		case WEATHER:  // WEATHER [Sunny|Stormy|Precipitation] (Duration for Stormy/Rainy)
			if (commandArgs[1].equalsIgnoreCase("sunny")) { thePlayer.getWorld().setStorm(false); }
			else if (commandArgs[1].equalsIgnoreCase("stormy")) { thePlayer.getWorld().setThundering(true); }
			else if (commandArgs[1].equalsIgnoreCase("precipitation")) { thePlayer.getWorld().setStorm(true); }
			break;

		case CAST: // CAST [POTION_TYPE] [DURATION] [AMPLIFIER]
			thePlayer.addPotionEffect(new PotionEffect(
					PotionEffectType.getByName(commandArgs[1]), Integer.valueOf(commandArgs[2]) * 20, Integer.valueOf(commandArgs[3])));
			break;


		case EFFECT:  // EFFECT [EFFECT_TYPE] (Location Bookmark)

			// PLAYER INTERACTION

		case LOOK: // ENG

			if (commandArgs[1].equalsIgnoreCase("CLOSE")) {
				if (!CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])).getTrait(LookClose.class).toggle())
					CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])).getTrait(LookClose.class).toggle();
			}
			if (commandArgs[1].equalsIgnoreCase("AWAY")) {
				if (CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])).getTrait(LookClose.class).toggle())
					CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])).getTrait(LookClose.class).toggle();
			}
			break;

		case GIVE:  // GIVE [Item:Data] [Amount] [ENCHANTMENT_TYPE]

			ItemStack giveItem = new ItemStack(Material.getMaterial(commandArgs[1].toUpperCase()));
			if (commandArgs.length > 1) giveItem.setAmount(Integer.valueOf(commandArgs[2]));
			else giveItem.setAmount(1);
			CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])).getBukkitEntity().getWorld()
			.dropItem(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])).getBukkitEntity().getLocation().add(
					CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])).getBukkitEntity().getLocation().getDirection().multiply(1.1)), giveItem);
			break;

		case TAKE:  // TAKE [Item] [Amount]   or  TAKE ITEM_IN_HAND  or  TAKE MONEY [Amount]

			if (commandArgs[1].equalsIgnoreCase("MONEY")) {
				double playerMoneyAmt = Denizen.econ.getBalance(thePlayer.getName());
				double amtToTake = Double.valueOf(commandArgs[2]);
				if (amtToTake > playerMoneyAmt) amtToTake = playerMoneyAmt;
				Denizen.econ.withdrawPlayer(thePlayer.getName(), amtToTake);

			}
			else if (commandArgs[1].equalsIgnoreCase("ITEMINHAND")) {
				thePlayer.setItemInHand(new ItemStack(Material.AIR));
			}
			else {
				ItemStack itemToTake = new ItemStack(Material.valueOf(commandArgs[1]));
				if (commandArgs.length > 2)	itemToTake.setAmount(Integer.valueOf(commandArgs[2]));
				else itemToTake.setAmount(1);
				thePlayer.getInventory().removeItem(itemToTake);
			}
			break;

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

			NPC theDenizenToWalk = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0]));
			Denizen.previousDenizenLocation.put(theDenizenToWalk, theDenizenToWalk.getBukkitEntity().getLocation());
			if (!commandArgs[1].isEmpty()) theDenizenToWalk.getAI().setDestination(theDenizenToWalk.getBukkitEntity().getLocation()
					.add(Double.parseDouble(commandArgs[2]), Double.parseDouble(commandArgs[3]), Double.parseDouble(commandArgs[1])));
			break;

		case WALKTO:  // WALKTO [Location Bookmark]

			NPC denizenWalking = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0]));
			Location walkLoc = getLocationBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])), commandArgs[1], "Location");
			Denizen.previousDenizenLocation.put(denizenWalking, denizenWalking.getBukkitEntity().getLocation());
			denizenWalking.getAI().setDestination(walkLoc);
			break;

		case RETURN:

			NPC theDenizenToReturn = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0]));
			if (Denizen.previousDenizenLocation.containsKey(theDenizenToReturn))
				theDenizenToReturn.getAI().setDestination(Denizen.previousDenizenLocation.
						get(theDenizenToReturn));
			break;

		case FINISH:

			plugin.getConfig().set("Players." + thePlayer.getName() + "." + rawqueArgs[1] + "." + "Completed", true);
			plugin.saveConfig();

			break;

		case REMEMBER:  // REMEMBER [CHAT|LOCATION|INVENTORY]



		case FOLLOW: // FOLLOW PLAYER|NOBODY

			NPC theDenizenFollowing = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0]));
			if (commandArgs[1].equalsIgnoreCase("PLAYER")) {
				theDenizenFollowing.getAI().setTarget(thePlayer, false);
			}
			if (commandArgs[1].equalsIgnoreCase("NOBODY")) {
				theDenizenFollowing.getAI().cancelDestination();
			}
			break;

		case RESPAWN:  // RESPAWN [Location Notable]

			Location respawnLoc = getLocationBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0])), commandArgs[1], "Location");
			NPC respawnDenizen = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0]));
			Denizen.previousDenizenLocation.put(respawnDenizen, respawnDenizen.getBukkitEntity().getLocation());
			respawnDenizen.getBukkitEntity().getWorld().playEffect(respawnDenizen.getBukkitEntity().getLocation(), Effect.STEP_SOUND, 2);
			respawnDenizen.despawn();
			respawnDenizen.spawn(respawnLoc);
			respawnDenizen.getBukkitEntity().getWorld().playEffect(respawnDenizen.getBukkitEntity().getLocation(), Effect.STEP_SOUND, 2);

			break;

		case PERMISS:  // PERMISS [Permission Node]

			Denizen.perms.playerAdd(thePlayer, commandArgs[1]);
			break;

		case EXECUTE:  // EXECUTE ASPLAYER [Command to Execute]

			String[] executeCommand = rawqueArgs[4].split(" ", 3);
			NPC theDenizenExecuting = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0]));
			if (commandArgs[1].equalsIgnoreCase("ASPLAYER")) {
				thePlayer.performCommand(executeCommand[2].replace("<PLAYER>", thePlayer.getName().replace("<WORLD>", thePlayer.getWorld().getName())));
			}
			if (commandArgs[1].equalsIgnoreCase("ASNPC")) {
				((Player) theDenizenExecuting.getBukkitEntity()).performCommand(executeCommand[2].replace("<PLAYER>", thePlayer.getName().replace("<WORLD>", thePlayer.getWorld().getName())));
			}
			if (commandArgs[1].equalsIgnoreCase("ASSERVER")) {
				plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), executeCommand[2].replace("<PLAYER>", thePlayer.getName().replace("<WORLD>", thePlayer.getWorld().getName())));
			}
			break;

			// SHOUT can be heard by players within 100 blocks.
			// WHISPER can only be heard by the player interacting with.
			// CHAT can be heard by the player, and players within 5 blocks.
			// NARRARATE can only be heard by the player and is not branded by the NPC.
			// ANNOUNCE can be heard by the entire server.

		case WHISPER:  // ZAP [Optional Step # to advance to]
		case NARRATE:  // ZAP [Optional Step # to advance to]
			if (rawqueArgs[4].split(" ", 2)[1].startsWith("*"))
				thePlayer.sendMessage("  " + rawqueArgs[4].split(" ", 2)[1].replace("*", "").replace("<PLAYER>", thePlayer.getName()).replace("<NPC>", CitizensAPI.getNPCRegistry().getNPC(Integer.parseInt(rawqueArgs[0])).getName()));
			else thePlayer.sendMessage(rawqueArgs[4].split(" ", 2)[1].replace("<PLAYER>", thePlayer.getName()).replace("<NPC>", CitizensAPI.getNPCRegistry().getNPC(Integer.parseInt(rawqueArgs[0])).getName()));
			break;

		case SHOUT:  // ZAP [Optional Step # to advance to]
		case CHAT:  // CHAT [Message]

			NPC theDenizenChatting = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(rawqueArgs[0]));
			if (rawqueArgs[4].split(" ", 2)[1].startsWith("*"))
				thePlayer.sendMessage("  " + rawqueArgs[4].split(" ", 2)[1].replace("*", ""));
			else thePlayer.sendMessage(plugin.getConfig().getString("npc_chat_to_player").replace("<TEXT>", rawqueArgs[4].split(" ", 2)[1]).replace("<PLAYER>", thePlayer.getDisplayName()).replace("<NPC>", CitizensAPI.getNPCRegistry().getNPC(Integer.parseInt(rawqueArgs[0])).getName()));
			for (Player eachPlayer : GetPlayersWithinRange(theDenizenChatting.getBukkitEntity().getLocation(), 
					theDenizenChatting.getBukkitEntity().getWorld(),
					plugin.getConfig().getInt("npc_to_player_chat_range_in_blocks", 15))) {
				if (eachPlayer != thePlayer) {
					if (rawqueArgs[4].split(" ", 2)[1].startsWith("*"))
						eachPlayer.sendMessage("    " + rawqueArgs[4].split(" ", 2)[1].replace("*", ""));
					else eachPlayer.sendMessage(plugin.getConfig().getString("npc_chat_to_player_bystander").replace("<TEXT>", rawqueArgs[4].split(" ", 2)[1]).replace("<PLAYER>", thePlayer.getDisplayName()).replace("<NPC>", CitizensAPI.getNPCRegistry().getNPC(Integer.parseInt(rawqueArgs[0])).getName()));
				}
			}
			break;

		case ANNOUNCE: // ANNOUNCE [Message]

			// NOTABLES

		case GRANT:  // ACHIEVE [Name of Achievement Notable to Grant]
		case BOUNCE:
			break;
		case CHANGE:
			break;
		case WAIT:
			break;
		case ENGAGE:
			break;
		case HINT:
			break;
		case NOD:
			break;
		default:
			break;

		}
	}


	public static Location getLocationBookmark(NPC theDenizen, String nameOfLocation, String BlockOrLocation) {

		List<String> locationList = null;

		if (BlockOrLocation.equalsIgnoreCase("block")) { 
			locationList = plugin.getConfig().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Block");	
		}
		if (BlockOrLocation.equalsIgnoreCase("location")) { 
			locationList = plugin.getConfig().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location");
		}
		String[] theLocation = null;
		Location locationBookmark = null;

		for (String thisLocation : locationList) {
			String theName = thisLocation.split(" ", 2)[0];
			if (theName.equalsIgnoreCase(nameOfLocation)) theLocation = thisLocation.split(" ", 2)[1].split(";");
		}
	
		if (theLocation != null && BlockOrLocation.equalsIgnoreCase("location")) {			
			locationBookmark = 
					new Location(plugin.getServer().getWorld(theLocation[0]),
							Double.parseDouble(theLocation[1]), Double.parseDouble(theLocation[2] + 1),
							Double.parseDouble(theLocation[3]), Float.parseFloat(theLocation[4]),
							Float.parseFloat(theLocation[5]));
		}
		
		else if (theLocation != null && BlockOrLocation.equalsIgnoreCase("block")) {
			locationBookmark = 
					new Location(plugin.getServer().getWorld(theLocation[0]),
							Double.parseDouble(theLocation[1]), Double.parseDouble(theLocation[2]),
							Double.parseDouble(theLocation[3]));
		}

		return locationBookmark;		

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