package net.aufdemrand.denizen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class Denizen extends JavaPlugin {


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be in-game to execute commands.");
			return true;
		}

		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Use /parrot help for command reference.");
			return true;
		}

		Player player = (Player) sender;

		if (args[0].equalsIgnoreCase("help")) {
			player.sendMessage(ChatColor.GOLD + "----- Denizen -----");
			player.sendMessage(ChatColor.GREEN + "/denizen clue" + ChatColor.GRAY
					+ " -- Sets the clue for the parrot.");
			return true;
		} 

		List<MetadataValue> NPCSelected = player.getMetadata("selected");

		NPC ThisNPC = CitizensAPI.getNPCManager().getNPC(player.getMetadata("selected").get(0).asInt());      // Gets NPC Selected

		if (NPCSelected.get(0) == null ) {
			player.sendMessage(ChatColor.RED + "You must have a parrot selected.");
			return true;
		}

		if (!ThisNPC.getTrait(Owner.class).getOwner().equals(player.getName())) {
			player.sendMessage(ChatColor.RED + "You must be the owner of the parrot to execute commands.");
			return true;
		}

		if (ThisNPC.getCharacter() == null || !ThisNPC.getCharacter().getName().equals("denizen")) {
			player.sendMessage(ChatColor.RED + "That command must be performed on a denizen!");
			return true;
		}

		// Commands

		if (args[0].equalsIgnoreCase("save")) {
			player.sendMessage("Settings saved.");
			saveConfig();
			return true;
		}
		else if (args[0].equalsIgnoreCase("clue")) {

			String NewClue = "";
			for(int i = 1; i < args.length; i++) { NewClue = NewClue + args[i] + " "; }

			getConfig().set(ThisNPC.getId() + ".clue", NewClue.toString());  // Set the key in the config.
			saveConfig();                   // Save the config.
			player.sendMessage(ChatColor.GREEN + "Clue saved.");   // Talk to the player.

			return true;
		}

		return true;
	}

	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
	}
	
	
	@Override
	public void onEnable() {

		setConfigurations();

	}


	// Configuration Nodes
	public static int PlayerChatRangeInBlocks;
		
	public void setConfigurations() {

		getConfig().options().copyDefaults(true);
		PlayerChatRangeInBlocks = getConfig().getInt("player_chat_range_in_blocks", 3);
		saveConfig();  
	}
	
	
}