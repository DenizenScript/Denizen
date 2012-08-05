package net.aufdemrand.denizen;

import java.util.List;

import net.aufdemrand.denizen.bookmarks.Bookmarks.BookmarkType;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;

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


	private Denizen plugin;

	public CommandExecuter(Denizen denizen) {
		plugin = denizen;
	}	


	public static enum Command {
		WAIT, ZAP, SPAWN, CHANGE, WEATHER, EFFECT, GIVE, TAKE, HEAL,
		TELEPORT, STRIKE, WALK, REMEMBER, RESPAWN, PERMISS, EXECUTE, SHOUT,
		WHISPER, CHAT, ANNOUNCE, GRANT, HINT, RETURN, LOOK, WALKTO, FINISH, 
		FOLLOW, CAST, NARRATE, ENGAGE, DISENGAGE,
		SWITCH, PRESS, HURT, REFUSE, WAITING, RESET, FAIL, SPAWNMOB, EMOTE, ATTACK, PLAYERTASK, RUNTASK, DROP
	} 


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

		case DROP:  // GIVE [Item:Data] [Amount] [ENCHANTMENT_TYPE]

			String[] thedropItem = plugin.getRequirements.splitItem(commandArgs[1]);
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

			String[] theItem = plugin.getRequirements.splitItem(commandArgs[1]);
			ItemStack giveItem = new ItemStack(Material.AIR);

			if (Character.isDigit(theItem[0].charAt(0))) {
				giveItem.setTypeId(Integer.valueOf(theItem[0]));
				if (theItem[1] != null) giveItem.getData().setData(Byte.valueOf(theItem[1]));
			}
			else giveItem.setType(Material.getMaterial(commandArgs[1].toUpperCase()));

			if (commandArgs[2] != null) giveItem.setAmount(Integer.valueOf(commandArgs[2]));
			else giveItem.setAmount(1);

			thePlayer.getInventory().addItem(giveItem);
			break;


		case TAKE:  // TAKE [Item] [Amount]   or  TAKE ITEM_IN_HAND  or  TAKE MONEY [Amount]
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

		case WALK:  // WALK Z(-NORTH(2)/+SOUTH(0)) X(-WEST(1)/+EAST(3)) Y (+UP/-DOWN)
			Denizen.previousNPCLoc.put(theDenizen, theDenizen.getBukkitEntity().getLocation());
			if (!commandArgs[1].isEmpty()) theDenizen.getAI().setDestination(theDenizen.getBukkitEntity().getLocation()
					.add(Double.parseDouble(commandArgs[2]), Double.parseDouble(commandArgs[3]), Double.parseDouble(commandArgs[1])));
			break;

		case WALKTO:  // WALKTO [Location Bookmark]
			Location walkLoc = plugin.bookmarks.get(theDenizen.getName(), commandArgs[1], BookmarkType.LOCATION);
			Denizen.previousNPCLoc.put(theDenizen, theDenizen.getBukkitEntity().getLocation());
			theDenizen.getAI().setDestination(walkLoc);
			break;

		case RETURN:
			if (Denizen.previousNPCLoc.containsKey(theDenizen))
				theDenizen.getAI().setDestination(Denizen.previousNPCLoc.get(theDenizen));
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
			Location respawnLoc = plugin.bookmarks.get(theDenizen.getName(), commandArgs[1], BookmarkType.LOCATION);
			Denizen.previousNPCLoc.put(theDenizen, theDenizen.getBukkitEntity().getLocation());

			theDenizen.despawn();
			theDenizen.spawn(respawnLoc);

			break;

		case PERMISS:  // PERMISS [Permission Node]
			plugin.perms.playerAdd(thePlayer, commandArgs[1]);
			break;

		case REFUSE:  // PERMISS [Permission Node]
			plugin.perms.playerRemove(thePlayer, commandArgs[1]);
			break;

		case EXECUTE:  // EXECUTE ASPLAYER [Command to Execute]
			String[] executeCommand = executeArgs[4].split(" ", 3);

			break;

			//     TYPE     BOOKMARK            DURATION   LEEWAY   RUNSCRIPT
		case PLAYERTASK: // LOCATION [Location Bookmark] [Duration] [Leeway] [Script to Trigger]

			/* LOCATION Listener */
			String theLocation = commandArgs[2];
			int theDuration = Integer.valueOf(commandArgs[3]);
			int theLeeway = Integer.valueOf(commandArgs[4]);
			String triggerScript = executeArgs[4].split(" ", 6)[5];
			plugin.scriptEngine.newLocationTask(thePlayer, theDenizen, theLocation, theDuration, theLeeway, triggerScript);
			break;

		case RUNTASK:
			plugin.scriptEngine.parseTaskScript(thePlayer, executeArgs[4].split(" ", 2)[1]);
			break;

		case ANNOUNCE: 
			break;

		case CHANGE: // CHANGE [Block Bookmark] [#:#|MATERIAL_TYPE]
			Location blockLoc = plugin.bookmarks.get(theDenizen.getName(), commandArgs[1], BookmarkType.BLOCK);

			String[] theChangeItem = plugin.getRequirements.splitItem(commandArgs[2]);

			if (Character.isDigit(theChangeItem[0].charAt(0))) {
				blockLoc.getBlock().setTypeId(Integer.valueOf(theChangeItem[0]));
				blockLoc.getBlock().setData(Byte.valueOf(theChangeItem[1]));
			}
			else blockLoc.getBlock().setType(Material.getMaterial(commandArgs[2].toUpperCase()));

			break;


			return;
		}



	}
