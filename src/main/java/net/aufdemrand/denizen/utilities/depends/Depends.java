package net.aufdemrand.denizen.utilities.depends;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.aufdemrand.denizen.scripts.commands.world.SchematicCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.citizensnpcs.Citizens;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.util.concurrent.ConcurrentHashMap;

public class Depends {

    public static WorldGuardPlugin worldGuard = null;
    public static WorldEditPlugin worldEdit = null;

    public static Citizens citizens = null;

    public static Economy economy = null;
    public static Permission permissions = null;
    public static Chat chat = null;

    public static boolean hasProgramAB = false;

    public static ProtocolManager protocolManager = null;

    public void initialize() {
        hasProgramAB = checkProgramAB();
        setupEconomy();
        setupPermissions();
        setupChat();
        setupWorldGuard();
        setupWorldEdit();
        setupCitizens();
        setupProtocolManager();
    }

    // Check if Program AB, used for reading Artificial Intelligence Markup
    // Language 2.0, is included as a dependency at Denizen/lib/Ab.jar
    private boolean checkProgramAB() {

        try { Class.forName("org.alicebot.ab.Bot"); }
        catch( ClassNotFoundException e ) { return false; }

        return true;
    }

    private boolean setupProtocolManager() {
        if (Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
           protocolManager = ProtocolLibrary.getProtocolManager();
        }

        return protocolManager != null;
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

    private boolean setupWorldEdit() {
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            return false;
        }
        worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit != null) {
            SchematicCommand.schematics = new ConcurrentHashMap<String, CuboidClipboard>();
        }
        return worldEdit != null;
    }

    private boolean setupCitizens() {
        citizens = (Citizens) Bukkit.getServer().getPluginManager().getPlugin("Citizens");
        return citizens != null;
    }

}
