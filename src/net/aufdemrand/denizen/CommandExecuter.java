package net.aufdemrand.denizen;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandExecuter {


	public static enum Command {
		WAIT, ZAP, SPAWN, CHANGE, WEATHER, EFFECT, GIVE, TAKE, HEAL,
		TELEPORT, STRIKE, WALK, REMEMBER, RESPAWN, PERMISS, EXECUTE, SHOUT,
		WHISPER, CHAT, ANNOUNCE, GRANT, HINT, RETURN, LOOK, WALKTO, FINISH, 
		FOLLOW, CAST, NARRATE,
		SWITCH, PRESS, HURT, REFUSE, WAITING, RESET, FAIL, SPAWNMOB, EMOTE
	} 

	private Denizen plugin;

	public void execute(Player thePlayer, String theStep) {

		// Syntax of theStep
		// 0 Denizen ID; 1 Script Name; 2 Step Number; 3 Time added to Queue; 4 Command

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		

		/* Break down information from theStep for use */
		String[] executeArgs = theStep.split(";");
		NPC theDenizen = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0]));
		String theScript = executeArgs[1];
		String currentStep = executeArgs[2];

		/* Populate 25 command arguments with values, rest with null */
		String[] commandArgs = new String[25];
		String[] argumentPopulator = executeArgs[4].split(" ");

		for (int count = 0; count < 25; count++) {
			if (argumentPopulator.length > count) commandArgs[count] = argumentPopulator[count];
			else commandArgs[count] = null;
		}

		if (commandArgs[0].startsWith("^")) commandArgs[0] = commandArgs[0].substring(1);
		
		/* Now to execute the command... */
		
		switch (Command.valueOf(commandArgs[0].toUpperCase())) {

		/* commandArgs [0] [1]      [2] [...]   */
		case ZAP:   /* ZAP (Step #)             */
			Denizen.getScript.zap(thePlayer, theScript, theStep, commandArgs[1]);
			break;


		case SPAWNMOB:
		case SPAWN:  /* SPAWN [ENTITY_TYPE] (AMOUNT) (Location Bookmark) */
			Denizen.getWorld.spawnMob(commandArgs[1], commandArgs[2], commandArgs[3], theDenizen);
			break;


		case SWITCH:  // SWITCH [Block Bookmark] ON|OFF
			Location switchLoc = Denizen.getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0])), commandArgs[1], "Block");
			if (switchLoc.getBlock().getType() == Material.LEVER) {
				World theWorld = switchLoc.getWorld();
				net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), switchLoc.getBlockX(), switchLoc.getBlockY(), switchLoc.getBlockZ(), null);
			}
			break;


		case PRESS:  // SWITCH [Block Bookmark] ON|OFF
			Location pressLoc = Denizen.getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0])), commandArgs[1], "Block");
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
			break;


		case LOOK: // ENG
			if (commandArgs[1].equalsIgnoreCase("CLOSE")) {
				if (!theDenizen.getTrait(LookClose.class).toggle())
					theDenizen.getTrait(LookClose.class).toggle();
			}
			else if (commandArgs[1].equalsIgnoreCase("AWAY")) {
				if (theDenizen.getTrait(LookClose.class).toggle())
					theDenizen.getTrait(LookClose.class).toggle();
			}
			else if (!commandArgs[1].equalsIgnoreCase("AWAY") && !commandArgs[1].equalsIgnoreCase("CLOSE")) {
				NPC denizenLooking = theDenizen;
				Location lookLoc = Denizen.getDenizen.getBookmark(theDenizen, commandArgs[1], "Location");
				denizenLooking.getBukkitEntity().getLocation().setPitch(lookLoc.getPitch());
				denizenLooking.getBukkitEntity().getLocation().setYaw(lookLoc.getYaw());
			}
			break;


		case GIVE:  // GIVE [Item:Data] [Amount] [ENCHANTMENT_TYPE]
			ItemStack giveItem = new ItemStack(Material.getMaterial(commandArgs[1].toUpperCase()));
			if (commandArgs.length > 1) giveItem.setAmount(Integer.valueOf(commandArgs[2]));
			else giveItem.setAmount(1);
			CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0])).getBukkitEntity().getWorld()
			.dropItem(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0])).getBukkitEntity().getLocation().add(
					CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0])).getBukkitEntity().getLocation().getDirection().multiply(1.1)), giveItem);
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
			break;


		case HURT:
			break;


		case TELEPORT:  // TELEPORT [Location Notable]
			thePlayer.teleport(Denizen.getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0])), commandArgs[1], "location"));


		case STRIKE:  // STRIKE    Strikes lightning on the player, with damage.
			thePlayer.getWorld().strikeLightning(thePlayer.getLocation());
			break;


		case WALK:  // WALK Z(-NORTH(2)/+SOUTH(0)) X(-WEST(1)/+EAST(3)) Y (+UP/-DOWN)
			Denizen.previousNPCLoc.put(theDenizen, theDenizen.getBukkitEntity().getLocation());
			if (!commandArgs[1].isEmpty()) theDenizen.getAI().setDestination(theDenizen.getBukkitEntity().getLocation()
					.add(Double.parseDouble(commandArgs[2]), Double.parseDouble(commandArgs[3]), Double.parseDouble(commandArgs[1])));
			break;


		case WALKTO:  // WALKTO [Location Bookmark]
			Location walkLoc = Denizen.getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0])), commandArgs[1], "Location");
			Denizen.previousNPCLoc.put(theDenizen, theDenizen.getBukkitEntity().getLocation());
			theDenizen.getAI().setDestination(walkLoc);
			break;


		case RETURN:
			if (Denizen.previousNPCLoc.containsKey(theDenizen))
				theDenizen.getAI().setDestination(Denizen.previousNPCLoc.get(theDenizen));
			break;


		case FINISH:

			int finishes = plugin.getAssignments().getInt("Players." + thePlayer.getName() + "." + executeArgs[1] + "." + "Completed", 0);
			finishes++;	
			plugin.getAssignments().set("Players." + thePlayer.getName() + "." + executeArgs[1] + "." + "Completed", finishes);
			plugin.saveAssignments();
			break;


		case FAIL:
			plugin.getAssignments().set("Players." + thePlayer.getName() + "." + executeArgs[1] + "." + "Failed", true);
			plugin.saveAssignments();
			break;


		case REMEMBER:  // REMEMBER [CHAT|LOCATION|INVENTORY]
			break;


		case FOLLOW: // FOLLOW PLAYER|NOBODY
			NPC theDenizenFollowing = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0]));
			if (commandArgs[1].equalsIgnoreCase("PLAYER")) {
				theDenizenFollowing.getAI().setTarget(thePlayer, false);
			}
			if (commandArgs[1].equalsIgnoreCase("NOBODY")) {
				theDenizenFollowing.getAI().cancelDestination();
			}
			break;


		case RESPAWN:  // RESPAWN [Location Notable]
			Location respawnLoc = Denizen.getDenizen.getBookmark(CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0])), commandArgs[1], "Location");
			Denizen.previousNPCLoc.put(theDenizen, theDenizen.getBukkitEntity().getLocation());

			theDenizen.getBukkitEntity().getWorld().playEffect(theDenizen.getBukkitEntity().getLocation(), Effect.STEP_SOUND, 2);
			theDenizen.despawn();
			theDenizen.spawn(respawnLoc);
			theDenizen.getBukkitEntity().getWorld().playEffect(theDenizen.getBukkitEntity().getLocation(), Effect.STEP_SOUND, 2);
			break;


		case PERMISS:  // PERMISS [Permission Node]
			Denizen.denizenPerms.playerAdd(thePlayer, commandArgs[1]);
			break;


		case REFUSE:  // PERMISS [Permission Node]
			Denizen.denizenPerms.playerRemove(thePlayer, commandArgs[1]);
			break;


		case EXECUTE:  // EXECUTE ASPLAYER [Command to Execute]
			String[] executeCommand = executeArgs[4].split(" ", 3);

			if (commandArgs[1].equalsIgnoreCase("ASPLAYER")) {
				thePlayer.performCommand(executeCommand[2]
						.replace("<PLAYER>", thePlayer.getName()
								.replace("<WORLD>", thePlayer.getWorld().getName())));
			}

			if (commandArgs[1].equalsIgnoreCase("ASNPC")) {

				((Player) theDenizen.getBukkitEntity()).setOp(true);
				((Player) theDenizen.getBukkitEntity()).performCommand(executeCommand[2]
						.replace("<PLAYER>", thePlayer.getName()
								.replace("<WORLD>", thePlayer.getWorld().getName())));
				((Player) theDenizen.getBukkitEntity()).setOp(false);
			}

			if (commandArgs[1].equalsIgnoreCase("ASSERVER")) {
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), executeCommand[2]
						.replace("<PLAYER>", thePlayer.getName()
								.replace("<WORLD>", thePlayer.getWorld().getName())));
			}
			break;


		case ANNOUNCE: 
		case NARRATE:  




		case WHISPER: 
		case EMOTE:
		case SHOUT:  
		case CHAT:  // CHAT [Message]
			List<String> textToChat = Denizen.scriptEngine.getMultilineText(executeArgs[4].split(" ", 2)[1]);
			List<String> AddedToPlayerQue = new ArrayList<String>();

			if (textToChat.size() > 1) {
				for (int z = 0; z < textToChat.size(); z++) {
					AddedToPlayerQue.add(commandArgs[0] + " " + textToChat.get(z));
				}

				if (Denizen.settings.MultipleLinesOfTextWaitForInteractDelay()) {
					Denizen.scriptEngine.injectToQue(theScript, Integer.valueOf(currentStep), thePlayer, theDenizen, AddedToPlayerQue);
					break;
				}
			}
			else AddedToPlayerQue.add(commandArgs[0] + " " + textToChat.get(0));

			for (String message : AddedToPlayerQue) 
				Denizen.getDenizen.talkToPlayer(theDenizen, thePlayer, message.split(" ", 2)[1], commandArgs[0]);

			break;


		case RESET: // RESET FINISH(ED) [Name of Script]  or  RESET FAIL(ED) [NAME OF SCRIPT]
			if (commandArgs[1].equalsIgnoreCase("FINISH") || commandArgs[1].equalsIgnoreCase("FINISHED")) {
				plugin.getAssignments().set("Players." + thePlayer.getName() + "." + theScript + "." + "Completed", 0);
				plugin.saveAssignments();
			}

			if (commandArgs[1].equalsIgnoreCase("FAIL") || commandArgs[1].equalsIgnoreCase("FAILED")) {
				plugin.getAssignments().set("Players." + thePlayer.getName() + "." + theScript + "." + "Failed", false);
				plugin.saveAssignments();
			}

			break;


		case CHANGE:
			break;


		case WAIT:
			/* This may be a bit hack-y, at least it seems like it to me.
			 * but, if it isn't broken.. you know what they say. 
			 */
			List<String> CurrentPlayerQue = new ArrayList<String>();
			if (Denizen.playerQue.get(thePlayer) != null) CurrentPlayerQue = Denizen.playerQue.get(thePlayer);
			Denizen.playerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add
			Long timeDelay = Long.parseLong(commandArgs[1]) * 1000;
			String timeWithDelay = String.valueOf(System.currentTimeMillis() + timeDelay);
			CurrentPlayerQue.add(1, "0;none;0;" + timeWithDelay + ";WAITING");						
			Denizen.playerQue.put(thePlayer, CurrentPlayerQue);
			break;


		case WAITING:
			// ...and we're waiting... mmmm... hack-y.
			break;


		default:
			break;
		}
	}

}
