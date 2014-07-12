package net.aufdemrand.denizen.utilities.depends;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.citizensnpcs.Citizens;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class Depends {

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

    private boolean setupCitizens() {
        citizens = (Citizens) Bukkit.getServer().getPluginManager().getPlugin("Citizens");
        return citizens != null;
    }

}
