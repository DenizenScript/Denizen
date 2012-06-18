package net.aufdemrand.denizen;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.ScriptEngine.Trigger;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandExecuter {


	public static enum Command {
		WAIT, ZAP, SPAWN, CHANGE, WEATHER, EFFECT, GIVE, TAKE, HEAL,
		TELEPORT, STRIKE, WALK, REMEMBER, RESPAWN, PERMISS, EXECUTE, SHOUT,
		WHISPER, CHAT, ANNOUNCE, GRANT, HINT, RETURN, LOOK, WALKTO, FINISH, 
		FOLLOW, CAST, NARRATE, ENGAGE, DISENGAGE,
		SWITCH, PRESS, HURT, REFUSE, WAITING, RESET, FAIL, SPAWNMOB, EMOTE, ATTACK, PLAYERTASK, RUNTASK, DROP
	} 

	private Denizen plugin;


	/*
	 * Executes a command defined in theStep (not to be confused with currentStep ;)
	 * 
	 * I am attempting to keep down the size of this method by branching out large
	 * sections of code into their own methods.
	 *
	 * These commands normally come from the playerQue or denizenQue, but don't have
	 * to necessarily, as long as the proper format is sent in theStep.
	 * 
	 * Syntax of theStep -- elements are divided by semi-colons.
	 * 0 Denizen ID; 1 Script Name; 2 Step Number; 3 Time added to Queue; 4 Command
	 */

	public void execute(Player thePlayer, String theStep) {

		// Syntax of theStep
		// 

		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		

		/* Break down information from theStep for use */
		String[] executeArgs = theStep.split(";");
		NPC theDenizen = null;
		if (!executeArgs[0].equalsIgnoreCase("NONE")) theDenizen = CitizensAPI.getNPCRegistry().getNPC(Integer.valueOf(executeArgs[0]));
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
			Denizen.getScript.zap(thePlayer, theScript, currentStep, commandArgs[1]);
			break;

		case ENGAGE:
			Denizen.engagedNPC.add(theDenizen);
			break;

		case DISENGAGE:
			if (Denizen.engagedNPC.contains(theDenizen)) Denizen.engagedNPC.remove(theDenizen);
			break;

		case SPAWNMOB:
		case SPAWN:  /* SPAWN [ENTITY_TYPE] (AMOUNT) (Location Bookmark) */
			Denizen.getWorld.spawnMob(commandArgs[1], commandArgs[2], commandArgs[3], theDenizen);
			break;


		case SWITCH:  // SWITCH [Block Bookmark] ON|OFF
			Location switchLoc = Denizen.getDenizen.getBookmark(theDenizen.getName(), commandArgs[1], "Block");
			if (switchLoc.getBlock().getType() == Material.LEVER) {
				World theWorld = switchLoc.getWorld();
				net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), switchLoc.getBlockX(), switchLoc.getBlockY(), switchLoc.getBlockZ(), null);
			}
			break;


		case PRESS:  // SWITCH [Block Bookmark] ON|OFF
			Location pressLoc = Denizen.getDenizen.getBookmark(theDenizen.getName(), commandArgs[1], "Block");
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
					PotionEffectType.getByName(commandArgs[1].toUpperCase()), Integer.valueOf(commandArgs[2]) * 20, Integer.valueOf(commandArgs[3])));
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
				Location lookLoc = Denizen.getDenizen.getBookmark(theDenizen.getName(), commandArgs[1], "Location");
				denizenLooking.getBukkitEntity().getLocation().setPitch(lookLoc.getPitch());
				denizenLooking.getBukkitEntity().getLocation().setYaw(lookLoc.getYaw());
			}
			break;

			
		case DROP:  // GIVE [Item:Data] [Amount] [ENCHANTMENT_TYPE]

			String[] thedropItem = Denizen.getRequirements.splitItem(commandArgs[1]);
			ItemStack dropItem = new ItemStack(Material.AIR);

			if (Character.isDigit(thedropItem[0].charAt(0))) {
				dropItem.setTypeId(Integer.valueOf(thedropItem[0]));
				if (thedropItem[1] != null) dropItem.getData().setData(Byte.valueOf(thedropItem[1]));
			}
			else dropItem.setType(Material.getMaterial(commandArgs[1].toUpperCase()));

			if (commandArgs[2] != null) dropItem.setAmount(Integer.valueOf(commandArgs[2]));
			else dropItem.setAmount(1);

			theDenizen.getBukkitEntity().getWorld().dropItem(thePlayer.getLocation(), dropItem);
			break;


		case GIVE:  // GIVE [Item:Data] [Amount] [ENCHANTMENT_TYPE]

			String[] theItem = Denizen.getRequirements.splitItem(commandArgs[1]);
			ItemStack giveItem = new ItemStack(Material.AIR);

			if (Character.isDigit(theItem[0].charAt(0))) {
				giveItem.setTypeId(Integer.valueOf(theItem[0]));
				if (theItem[1] != null) giveItem.getData().setData(Byte.valueOf(theItem[1]));
			}
			else giveItem.setType(Material.getMaterial(commandArgs[1].toUpperCase()));

			if (commandArgs[2] != null) giveItem.setAmount(Integer.valueOf(commandArgs[2]));
			else giveItem.setAmount(1);

			thePlayer.getWorld().dropItem(thePlayer.getLocation(), giveItem);
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

				String[] theTakeItem = Denizen.getRequirements.splitItem(commandArgs[1]);
				ItemStack itemToTake = new ItemStack(Material.AIR);

				if (Character.isDigit(theTakeItem[0].charAt(0))) {
					itemToTake.setTypeId(Integer.valueOf(theTakeItem[0]));
					if (theTakeItem[1] != null) itemToTake.getData().setData(Byte.valueOf(theTakeItem[1]));
				}
				else itemToTake.setType(Material.getMaterial(commandArgs[1].toUpperCase()));

				if (commandArgs[2] != null) itemToTake.setAmount(Integer.valueOf(commandArgs[2]));
				else itemToTake.setAmount(1);

				thePlayer.getInventory().removeItem(itemToTake);
			}

			break;


		case HEAL:  // HEAL (# of Health)
			int health = 1;
			if (commandArgs[1] != null) health = Integer.valueOf(commandArgs[1]);
			((LivingEntity) thePlayer).setHealth(thePlayer.getHealth() + health);
			break;


		case HURT:  // HURT (# of Health)
			int damage = 1;
			if (commandArgs[1] != null) damage = Integer.valueOf(commandArgs[1]);
			if (theDenizen != null)	thePlayer.damage(damage, theDenizen.getBukkitEntity());
			else thePlayer.damage(damage);
			break;


		case TELEPORT:  // TELEPORT [Location Notable]
			thePlayer.teleport(Denizen.getDenizen.getBookmark(theDenizen.getName(), commandArgs[1], "location"));


		case STRIKE:  // STRIKE    Strikes lightning on the player, with damage.
			thePlayer.getWorld().strikeLightning(thePlayer.getLocation());
			break;


		case WALK:  // WALK Z(-NORTH(2)/+SOUTH(0)) X(-WEST(1)/+EAST(3)) Y (+UP/-DOWN)
			Denizen.previousNPCLoc.put(theDenizen, theDenizen.getBukkitEntity().getLocation());
			if (!commandArgs[1].isEmpty()) theDenizen.getAI().setDestination(theDenizen.getBukkitEntity().getLocation()
					.add(Double.parseDouble(commandArgs[2]), Double.parseDouble(commandArgs[3]), Double.parseDouble(commandArgs[1])));
			break;


		case WALKTO:  // WALKTO [Location Bookmark]
			Location walkLoc = Denizen.getDenizen.getBookmark(theDenizen.getName(), commandArgs[1], "Location");
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
			plugin.getSaves().set("Players." + thePlayer.getName() + "." + executeArgs[1] + "." + "Completed", finishes);
			plugin.saveSaves();
			break;


		case FAIL:
			plugin.getSaves().set("Players." + thePlayer.getName() + "." + executeArgs[1] + "." + "Failed", true);
			plugin.saveSaves();
			break;


		case REMEMBER:  // REMEMBER [CHAT|LOCATION|INVENTORY]
			break;


		case FOLLOW: // FOLLOW PLAYER|NOBODY
			if (commandArgs[1].equalsIgnoreCase("PLAYER")) {
				theDenizen.getAI().setTarget(thePlayer, false);
			}
			if (commandArgs[1].equalsIgnoreCase("NOBODY")) {
				theDenizen.getAI().cancelDestination();
			}
			break;

		case ATTACK: // FOLLOW PLAYER|NOBODY
			if (commandArgs[1].equalsIgnoreCase("PLAYER")) {
				theDenizen.getAI().setTarget(thePlayer, true);
			}
			if (commandArgs[1].equalsIgnoreCase("NOBODY")) {
				theDenizen.getAI().cancelDestination();
			}
			break;



		case RESPAWN:  // RESPAWN [Location Notable]
			Location respawnLoc = Denizen.getDenizen.getBookmark(theDenizen.getName(), commandArgs[1], "Location");
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

					 //     TYPE     BOOKMARK            DURATION   LEEWAY   RUNSCRIPT
		case PLAYERTASK: // LOCATION [Location Bookmark] [Duration] [Leeway] [Script to Trigger]
			
			/* LOCATION Listener */
			String theLocation = commandArgs[2];
			int theDuration = Integer.valueOf(commandArgs[3]);
			int theLeeway = Integer.valueOf(commandArgs[4]);
			String triggerScript = executeArgs[4].split(" ", 6)[5];
			Denizen.scriptEngine.newLocationTask(thePlayer, theDenizen, theLocation, theDuration, theLeeway, triggerScript);
			break;

		case RUNTASK:
			Denizen.scriptEngine.parseScript(null, thePlayer, executeArgs[4].split(" ", 2)[1], null, Trigger.TASK);
			break;
			
		case ANNOUNCE: 
			break;

		case NARRATE:  
		case WHISPER: 
		case EMOTE:
		case SHOUT:  
		case CHAT:  // CHAT|WHISPER|EMOTE|SHOUT|NARRATE [Message]

			/* 
			 * I had to take out the feature for multiline text following the script delay. It was getting too messy!
			 * Hopefully nobody will notice ;) ...but I'm sure they will, so I will put that off for another day.
			 */

			/* Format the text for player and bystander, and turn into multiline if necessary */

			String[] formattedText = Denizen.scriptEngine.formatChatText(executeArgs[4].split(" ", 2)[1], commandArgs[0], thePlayer, theDenizen);

			List<String> playerText = Denizen.scriptEngine.getMultilineText(formattedText[0]);
			List<String> bystanderText = Denizen.scriptEngine.getMultilineText(formattedText[1]);

			/* Spew the text to the world. */

			if (!playerText.isEmpty()) {
				for (String text : playerText) { /* First playerText */
					Denizen.getDenizen.talkToPlayer(theDenizen, thePlayer, text, null, commandArgs[0]);
				}
			}

			if (!bystanderText.isEmpty()) {
				for (String text : bystanderText) { /* now bystanderText */
					if (!playerText.isEmpty()) Denizen.getDenizen.talkToPlayer(theDenizen, thePlayer, "shhh...don't speak!", text, commandArgs[0]);
					else Denizen.getDenizen.talkToPlayer(theDenizen, thePlayer, null, text, commandArgs[0]);
				}
			}
			break;


		case RESET: // RESET FINISH(ED) [Name of Script]  or  RESET FAIL(ED) [NAME OF SCRIPT]
			String executeScript;
			if (commandArgs[2] == null) executeScript=theScript; else executeScript=executeArgs[4].split(" ", 3)[2];
			if (commandArgs[1].equalsIgnoreCase("FINISH") || commandArgs[1].equalsIgnoreCase("FINISHED")) {
				plugin.getSaves().set("Players." + thePlayer.getName() + "." + executeScript + "." + "Completed", 0);
				plugin.saveSaves();
			}

			if (commandArgs[1].equalsIgnoreCase("FAIL") || commandArgs[1].equalsIgnoreCase("FAILED")) {
				plugin.getSaves().set("Players." + thePlayer.getName() + "." + executeScript + "." + "Failed", false);
				plugin.saveSaves();
			}

			break;


		case CHANGE: // CHANGE [Block Bookmark] [#:#|MATERIAL_TYPE]
			Location blockLoc = Denizen.getDenizen.getBookmark(theDenizen.getName(), commandArgs[1], "Block");

			String[] theChangeItem = Denizen.getRequirements.splitItem(commandArgs[2]);

			if (Character.isDigit(theChangeItem[0].charAt(0))) {
				blockLoc.getBlock().setTypeId(Integer.valueOf(theChangeItem[0]));
				blockLoc.getBlock().setData(Byte.valueOf(theChangeItem[1]));
			}
			else blockLoc.getBlock().setType(Material.getMaterial(commandArgs[2].toUpperCase()));

			break;


		case WAIT:
			/* 
			 * This may be a bit hack-y, at least it seems like it to me.
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

		return;
	}



}
