package com.denizenscript.denizen.utilities.depends;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.citizensnpcs.Citizens;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Depends {

    public static Citizens citizens = null;

    public static Economy economy = null;
    public static Permission permissions = null;
    public static Chat chat = null;
    public static Plugin vault = null;

    public static void initialize() {
        vault = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        setupBungee();
        setupEconomy();
        setupPermissions();
        setupChat();
        setupCitizens();
    }

    public static void setupBungee() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(DenizenAPI.getCurrentInstance(), "BungeeCord");
    }

    public static void bungeeSendPlayer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(DenizenAPI.getCurrentInstance(), "BungeeCord", out.toByteArray());
    }

    public static boolean setupEconomy() {
        if (vault == null || !vault.isEnabled()) {
            return false;
        }
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }
            economy = rsp.getProvider();
        }
        catch (Exception e) {
        }
        return economy != null;
    }

    public static boolean setupChat() {
        if (vault == null || !vault.isEnabled()) {
            return false;
        }
        try {
            RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
            chat = rsp.getProvider();
        }
        catch (Exception e) {
        }
        return chat != null;
    }

    public static boolean setupPermissions() {
        if (vault == null || !vault.isEnabled()) {
            return false;
        }
        try {
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            permissions = rsp.getProvider();
        }
        catch (Exception e) {
        }
        return permissions != null;
    }

    public static boolean setupCitizens() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Citizens");
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }
        try {
            citizens = (Citizens) Bukkit.getServer().getPluginManager().getPlugin("Citizens");
        }
        catch (Exception e) {
        }
        return citizens != null;
    }
}
