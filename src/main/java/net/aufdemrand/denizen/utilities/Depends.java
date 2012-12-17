package net.aufdemrand.denizen.utilities;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.citizensnpcs.Citizens;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Depends {

	public static WorldGuardPlugin worldGuard = null;
	public static Citizens citizens = null;
	
    public static Economy economy = null;
    public static Permission permissions = null;
    public static Chat chat = null;
    
    public Depends() {
        setupEconomy();
        setupPermissions();
        setupChat();
        setupWorldGuard();
        setupCitizens();
    }

    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        try {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        } catch (Exception e) { }
        return economy != null;
    }

    private boolean setupChat() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        try { RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        } catch (Exception e) { }
        return chat != null;
    }

    private boolean setupPermissions() {
    	if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
    	try {
    	RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        permissions = rsp.getProvider();
    	} catch (Exception e) { }
        return permissions != null;
    }
    
    private boolean setupWorldGuard() {
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            return false;
        }
    	worldGuard = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
    	return worldGuard != null;
    }
	
    private boolean setupCitizens() {
        citizens = (Citizens) Bukkit.getServer().getPluginManager().getPlugin("Citizens");
        return citizens != null;
    }
	
}
