package net.aufdemrand.denizen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import net.aufdemrand.denizen.DenizenCharacter;
import net.aufdemrand.denizen.DenizenListener;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.trait.trait.Owner;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
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
			sender.sendMessage(ChatColor.RED + "Use /denizen help for command reference.");
			return true;
		}

		Player player = (Player) sender;

		if (args[0].equalsIgnoreCase("help")) {
			player.sendMessage(ChatColor.GOLD + "----- Denizen -----");
			return true;
		} 

		if (args[0].equalsIgnoreCase("debug")) {

			if (this.DebugMode==false) {DebugMode = true; 
			player.sendMessage(ChatColor.GREEN + "Debug mode ON.");   // Talk to the player.
			return true;
			}

			else if (this.DebugMode==true) {DebugMode = false; 
			player.sendMessage(ChatColor.GREEN + "Debug mode OFF.");   // Talk to the player.
			return true;
			}

			return true;
		}

		if (player.getMetadata("selected").isEmpty()) { 
			player.sendMessage(ChatColor.RED + "You must have a Denizen selected.");
			return true;
		}

		NPC ThisNPC = CitizensAPI.getNPCManager().getNPC(player.getMetadata("selected").get(0).asInt());      // Gets NPC Selected


		if (!ThisNPC.getTrait(Owner.class).getOwner().equals(player.getName())) {
			player.sendMessage(ChatColor.RED + "You must be the owner of the denizen to execute commands.");
			return true;
		}

		if (ThisNPC.getCharacter() == null || !ThisNPC.getCharacter().getName().equals("denizen")) {
			player.sendMessage(ChatColor.RED + "That command must be performed on a denizen!");
			return true;
		}

		// Commands

		if (args[0].equalsIgnoreCase("show")) {
			player.sendMessage(PlayerQue.toString());
			return true;
		}

		else if (args[0].equalsIgnoreCase("assign")) {
			player.sendMessage(ChatColor.GREEN + "Assigned.");   // Talk to the player.
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

		CitizensAPI.getCharacterManager().registerCharacter(new CharacterFactory(DenizenCharacter.class).withName("denizen"));

		getServer().getPluginManager().registerEvents(new DenizenListener(this), this);

		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {

				if (!PlayerQue.isEmpty()) {
					for (Map.Entry<Player, List<String>> theEntry : PlayerQue.entrySet()) {

						Player thePlayer = theEntry.getKey();
						List<String> theEntireScript = theEntry.getValue();

						if (!theEntireScript.isEmpty()) {

							String thisTime = theEntireScript.get(0);
							theEntireScript.remove(0);

							PlayerQue.put(thePlayer, theEntireScript);

							ExecuteScript(thePlayer, thisTime);
						}

						
					}
				}

			}
		}, InteractDelayInTicks, InteractDelayInTicks);


	}


	public void ExecuteScript(Player thePlayer, String StepToExecute) {

		thePlayer.sendMessage(StepToExecute);

		return;
	}


	// Configuration Nodes
	public int PlayerChatRangeInBlocks;
	public int InteractDelayInTicks;	
	public String TalkToNPCString;
	public Boolean DebugMode;

	public static Map<Player, List<String>> PlayerQue = new HashMap<Player, List<String>>();

	public void setConfigurations() {
		// getConfig().options().copyDefaults(true);

		PlayerChatRangeInBlocks = getConfig().getInt("player_chat_range_in_blocks", 3);
		InteractDelayInTicks = getConfig().getInt("interact_delay_in_ticks", 5);
		TalkToNPCString = getConfig().getString("talk_to_npc_string", "You say to <NPC>, '<TEXT>'");
		DebugMode = getConfig().getBoolean("debug_mode", false);

		saveConfig();  
	}





}