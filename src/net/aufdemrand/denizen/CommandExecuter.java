package net.aufdemrand.denizen;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.utilities.GetDenizen;
import net.aufdemrand.denizen.utilities.GetPlayer;
import net.aufdemrand.denizen.utilities.GetScript;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandExecuter {

	Plugin plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");	
	GetScript getScripts = new GetScript();
	GetPlayer getPlayer = new GetPlayer();
	GetDenizen getDenizen = new GetDenizen();
	
	public static enum Command {
		WAIT, ZAP, SPAWN, CHANGE, WEATHER, EFFECT, GIVE, TAKE, HEAL,
		TELEPORT, STRIKE, WALK, REMEMBER, RESPAWN, PERMISS, EXECUTE, SHOUT,
		WHISPER, CHAT, ANNOUNCE, GRANT, HINT, RETURN, LOOK, WALKTO, FINISH, FOLLOW, CAST, NARRATE,
		SWITCH, PRESS, HURT, REFUSE, WAITING, RESET, FAIL
	} 

	
	
	public void execute(Player thePlayer, String theStep) {

		// Syntax of theStep
		// 0 Denizen ID; 1 Script Name; 2 Step Number; 3 Time added to Queue; 4 Command

		String[] executerArgs = theStep.split(";");
		String[] commandArgs = executerArgs[4].split(" ");
		if (commandArgs[0].startsWith("^")) commandArgs[0] = commandArgs[0].substring(1);
		switch (Command.valueOf(commandArgs[0].toUpperCase())) {

		case ZAP:  // ZAP [Optional Step # to advance to]
			if (commandArgs.length == 1) { 
				plugin.getConfig().set("Players." + thePlayer.getName() + "." + executerArgs[1] + ".Current Step", Integer.parseInt(executerArgs[2]) + 1);
				plugin.saveConfig();
			}
			else { 
				plugin.getConfig().set("Players." + thePlayer.getName() + "." + executerArgs[1] + ".Current Step", Integer.parseInt(commandArgs[1])); 
				plugin.saveConfig();
			}
			break;

		case SPAWN:  // SPAWN [MOB NAME] [AMOUNT] (Location Bookmark)

			Location theSpawnLoc = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])).getBukkitEntity().getLocation();
			if (commandArgs.length > 3) theSpawnLoc = getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])), commandArgs[3], "Location");
			if (theSpawnLoc != null) {
				for (int cx = 1; cx < Integer.valueOf("commandArgs[2]"); cx++) {
					thePlayer.getWorld().spawnCreature(theSpawnLoc, EntityType.valueOf(commandArgs[1]));	
				}
			}
			break;

		case SWITCH:  // SWITCH [Block Bookmark] ON|OFF
			Location switchLoc = getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])), commandArgs[1], "Block");
			if (switchLoc.getBlock().getType() == Material.LEVER) {
				World theWorld = switchLoc.getWorld();
				net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), switchLoc.getBlockX(), switchLoc.getBlockY(), switchLoc.getBlockZ(), null);
			}
			break;

		case PRESS:  // SWITCH [Block Bookmark] ON|OFF
			Location pressLoc = getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])), commandArgs[1], "Block");
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
				if (!CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])).getTrait(LookClose.class).toggle())
					CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])).getTrait(LookClose.class).toggle();
			}
			else if (commandArgs[1].equalsIgnoreCase("AWAY")) {
				if (CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])).getTrait(LookClose.class).toggle())
					CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])).getTrait(LookClose.class).toggle();
			}
			else if (!commandArgs[1].equalsIgnoreCase("AWAY") && !commandArgs[1].equalsIgnoreCase("CLOSE")) {
				NPC denizenLooking = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0]));
				Location lookLoc = getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])), commandArgs[1], "Location");
				denizenLooking.getBukkitEntity().getLocation().setPitch(lookLoc.getPitch());
				denizenLooking.getBukkitEntity().getLocation().setYaw(lookLoc.getYaw());
			}
			break;

		case GIVE:  // GIVE [Item:Data] [Amount] [ENCHANTMENT_TYPE]
			ItemStack giveItem = new ItemStack(Material.getMaterial(commandArgs[1].toUpperCase()));
			if (commandArgs.length > 1) giveItem.setAmount(Integer.valueOf(commandArgs[2]));
			else giveItem.setAmount(1);
			CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])).getBukkitEntity().getWorld()
			.dropItem(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])).getBukkitEntity().getLocation().add(
					CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])).getBukkitEntity().getLocation().getDirection().multiply(1.1)), giveItem);
			break;

		case TAKE:  // TAKE [Item] [Amount]   or  TAKE ITEM_IN_HAND  or  TAKE MONEY [Amount]

			if (commandArgs[1].equalsIgnoreCase("MONEY")) {
				double playerMoneyAmt = Denizen.denizenEcon.getBalance(thePlayer.getName());
				double amtToTake = Double.valueOf(commandArgs[2]);
				if (amtToTake > playerMoneyAmt) amtToTake = playerMoneyAmt;
				Denizen.denizenEcon.withdrawPlayer(thePlayer.getName(), amtToTake);

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



		case HURT:



		case TELEPORT:  // TELEPORT [Location Notable]

			thePlayer.teleport(getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])), commandArgs[1], "location"));

		case STRIKE:  // STRIKE    Strikes lightning on the player, with damage.

			thePlayer.getWorld().strikeLightning(thePlayer.getLocation());
			break;

			// DENIZEN INTERACTION

		case WALK:  // WALK Z(-NORTH(2)/+SOUTH(0)) X(-WEST(1)/+EAST(3)) Y (+UP/-DOWN)

			NPC theDenizenToWalk = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0]));
			Denizen.previousDenizenLocation.put(theDenizenToWalk, theDenizenToWalk.getBukkitEntity().getLocation());
			if (!commandArgs[1].isEmpty()) theDenizenToWalk.getAI().setDestination(theDenizenToWalk.getBukkitEntity().getLocation()
					.add(Double.parseDouble(commandArgs[2]), Double.parseDouble(commandArgs[3]), Double.parseDouble(commandArgs[1])));
			break;

		case WALKTO:  // WALKTO [Location Bookmark]

			NPC denizenWalking = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0]));
			Location walkLoc = getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])), commandArgs[1], "Location");
			Denizen.previousDenizenLocation.put(denizenWalking, denizenWalking.getBukkitEntity().getLocation());
			denizenWalking.getAI().setDestination(walkLoc);
			break;

		case RETURN:

			NPC theDenizenToReturn = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0]));
			if (Denizen.previousDenizenLocation.containsKey(theDenizenToReturn))
				theDenizenToReturn.getAI().setDestination(Denizen.previousDenizenLocation.
						get(theDenizenToReturn));
			break;

		case FINISH:
			plugin.getConfig().set("Players." + thePlayer.getName() + "." + executerArgs[1] + "." + "Completed", true);
			plugin.saveConfig();
			break;

		case FAIL:
			plugin.getConfig().set("Players." + thePlayer.getName() + "." + executerArgs[1] + "." + "Failed", true);
			plugin.saveConfig();
			break;

		case REMEMBER:  // REMEMBER [CHAT|LOCATION|INVENTORY]
			break;

		case FOLLOW: // FOLLOW PLAYER|NOBODY

			NPC theDenizenFollowing = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0]));
			if (commandArgs[1].equalsIgnoreCase("PLAYER")) {
				theDenizenFollowing.getAI().setTarget(thePlayer, false);
			}
			if (commandArgs[1].equalsIgnoreCase("NOBODY")) {
				theDenizenFollowing.getAI().cancelDestination();
			}
			break;

		case RESPAWN:  // RESPAWN [Location Notable]

			Location respawnLoc = getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0])), commandArgs[1], "Location");
			NPC respawnDenizen = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0]));
			Denizen.previousDenizenLocation.put(respawnDenizen, respawnDenizen.getBukkitEntity().getLocation());
			respawnDenizen.getBukkitEntity().getWorld().playEffect(respawnDenizen.getBukkitEntity().getLocation(), Effect.STEP_SOUND, 2);
			respawnDenizen.despawn();
			respawnDenizen.spawn(respawnLoc);
			respawnDenizen.getBukkitEntity().getWorld().playEffect(respawnDenizen.getBukkitEntity().getLocation(), Effect.STEP_SOUND, 2);

			break;

		case PERMISS:  // PERMISS [Permission Node]

			Denizen.denizenPerms.playerAdd(thePlayer, commandArgs[1]);
			break;

		case REFUSE:  // PERMISS [Permission Node]

			Denizen.denizenPerms.playerRemove(thePlayer, commandArgs[1]);
			break;


		case EXECUTE:  // EXECUTE ASPLAYER [Command to Execute]

			String[] executeCommand = executerArgs[4].split(" ", 3);
			NPC theDenizenExecuting = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0]));
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
			if (executerArgs[4].split(" ", 2)[1].startsWith("*"))
				thePlayer.sendMessage("  " + executerArgs[4].split(" ", 2)[1].replace("*", "").replace("<PLAYER>", thePlayer.getName()).replace("<NPC>", CitizensAPI.getNPCRegistry().getNPC(Integer.parseInt(executerArgs[0])).getName()));
			else thePlayer.sendMessage(executerArgs[4].split(" ", 2)[1].replace("<PLAYER>", thePlayer.getName()).replace("<NPC>", CitizensAPI.getNPCRegistry().getNPC(Integer.parseInt(executerArgs[0])).getName()));
			break;

		case SHOUT:  // ZAP [Optional Step # to advance to]
		case CHAT:  // CHAT [Message]

			NPC theDenizenChatting = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executerArgs[0]));
			if (executerArgs[4].split(" ", 2)[1].startsWith("*"))
				thePlayer.sendMessage("  " + executerArgs[4].split(" ", 2)[1].replace("*", ""));
			else thePlayer.sendMessage(plugin.getConfig().getString("npc_chat_to_player").replace("<TEXT>", executerArgs[4].split(" ", 2)[1]).replace("<PLAYER>", thePlayer.getDisplayName()).replace("<NPC>", CitizensAPI.getNPCRegistry().getNPC(Integer.parseInt(executerArgs[0])).getName()));
			for (Player eachPlayer : getPlayer.getInRange(theDenizenChatting,
					plugin.getConfig().getInt("npc_to_player_chat_range_in_blocks", 15))) {
				if (eachPlayer != thePlayer) {
					if (executerArgs[4].split(" ", 2)[1].startsWith("*"))
						eachPlayer.sendMessage("    " + executerArgs[4].split(" ", 2)[1].replace("*", ""));
					else eachPlayer.sendMessage(plugin.getConfig().getString("npc_chat_to_player_bystander").replace("<TEXT>", executerArgs[4].split(" ", 2)[1]).replace("<PLAYER>", thePlayer.getDisplayName()).replace("<NPC>", CitizensAPI.getNPCRegistry().getNPC(Integer.parseInt(executerArgs[0])).getName()));
				}
			}
			break;

		case ANNOUNCE: // ANNOUNCE [Message]

			// NOTABLES

		case RESET: // RESET FINISH(ED) [Name of Script]  or  RESET FAIL(ED) [NAME OF SCRIPT]

			String nameOfScript = executerArgs[4].split(" ", 3)[2];

			if (commandArgs[1].equalsIgnoreCase("FINISH") || commandArgs[1].equalsIgnoreCase("FINISHED")) {
				plugin.getConfig().set("Players." + thePlayer.getName() + "." + nameOfScript + "." + "Completed", false);
				plugin.saveConfig();
			}
			if (commandArgs[1].equalsIgnoreCase("FAIL") || commandArgs[1].equalsIgnoreCase("FAILED")) {
				plugin.getConfig().set("Players." + thePlayer.getName() + "." + nameOfScript + "." + "Failed", false);
				plugin.saveConfig();
			}

			break;

			
		case CHANGE:
			break;
		case WAIT:

			List<String> CurrentPlayerQue = new ArrayList<String>();
			if (Denizen.playerQue.get(thePlayer) != null) CurrentPlayerQue = Denizen.playerQue.get(thePlayer);
			Denizen.playerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add
			Long timeDelay = Long.parseLong(commandArgs[1]) * 1000;
			String timeWithDelay = String.valueOf(System.currentTimeMillis() + timeDelay);
			CurrentPlayerQue.add(1, "0;none;0;" + timeWithDelay + ";WAITING");						
			Denizen.playerQue.put(thePlayer, CurrentPlayerQue);

			break;

		case WAITING:

			// ...and we're waiting.

			break;
		default:
			break;


		}
	}

	
}
