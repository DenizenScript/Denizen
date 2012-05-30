package net.aufdemrand.denizen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import net.aufdemrand.denizen.DenizenCharacter;
import net.aufdemrand.denizen.DenizenListener;
import net.aufdemrand.denizen.InteractScriptEngine;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Denizen extends JavaPlugin {

	public static Economy econ = null;
	public static Permission perms = null;
	public static Map<Player, List<String>> playerQue = new HashMap<Player, List<String>>();
	public static Map<NPC, Location> previousDenizenLocation = new HashMap<NPC, Location>(); 
	public static Map<Player, Long> interactCooldown = new HashMap<Player, Long>(); 
	public static Boolean DebugMode = false;
	public static List<Block> buttonHandlerList;

	@Override
	public void onEnable() {

		if (!setupEconomy() ) {
			getLogger().log(Level.SEVERE, String.format("[%s] - Disabled due to no Vault-compatible Economy Plugin found! Install an economy system!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;  }
		setupPermissions();
		reloadConfig();
		reloadScripts();
		CitizensAPI.getCharacterManager().registerCharacter(new CharacterFactory(DenizenCharacter.class).withName("denizen"));
		getServer().getPluginManager().registerEvents(new DenizenListener(this), this);

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() { CommandQue(); }
		}, getConfig().getInt("interact_delay_in_ticks", 10), getConfig().getInt("interact_delay_in_ticks", 10));

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() { ScheduleScripts(); }
		}, 1, 1000);

	}


		protected void ScheduleScripts() {

		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);
		if (DenizenNPCs.isEmpty()) return;
		List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (NPC aDenizen : DenizenList) {
			if (aDenizen.isSpawned())	{
				int denizenTime = Math.round(aDenizen.getBukkitEntity().getWorld().getTime() / 1000);
				List<String> denizenActivities = getConfig().getStringList("Denizens." + aDenizen.getName() + ".Scheduled Activities");
				if (!denizenActivities.isEmpty()) {
					for (String activity : denizenActivities) {
						if (activity.startsWith(String.valueOf(denizenTime))) {
							getServer().broadcastMessage("Updating Activity Script for " + aDenizen.getName());
							getConfig().set("Denizens." + aDenizen.getName() + ".Active Activity Script", activity.split(" ", 2)[1]);
							saveConfig();
						}
					}
				}
			}
		}
	}


	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
		Bukkit.getServer().getScheduler().cancelTasks(this);
	}




	protected void CommandQue() {

		boolean instantCommand = false;
		if (!playerQue.isEmpty()) {	
			for (Map.Entry<Player, List<String>> theEntry : playerQue.entrySet()) {
				if (!theEntry.getValue().isEmpty()) {
					if (Long.valueOf(theEntry.getValue().get(0).split(";")[3]) < System.currentTimeMillis()) {
						do {
							InteractScriptEngine.commandExecuter(theEntry.getKey(), theEntry.getValue().get(0));
							instantCommand = false;
							if (theEntry.getValue().get(0).split(";")[4].startsWith("^")) instantCommand = true;
							theEntry.getValue().remove(0);
							playerQue.put(theEntry.getKey(), theEntry.getValue());
						} while (instantCommand == true);
					}
				}
			}

		}
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) return false;
		econ = rsp.getProvider();
		return econ != null;
	}



	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}



	// SCRIPTS CONFIGURATION METHODS


	private FileConfiguration customConfig = null;
	private File customConfigFile = null;


	public void reloadScripts() {
		if (customConfigFile == null) {
			customConfigFile = new File(getDataFolder(), "scripts.yml");
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = getResource("scripts.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			customConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getScripts() {
		if (customConfig == null) {
			reloadScripts();
		}
		return customConfig;
	}

	public void saveScripts() {
		if (customConfig == null || customConfigFile == null) {
			return;
		}
		try {
			customConfig.save(customConfigFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save saves to " + customConfigFile, ex);
		}
	}

}