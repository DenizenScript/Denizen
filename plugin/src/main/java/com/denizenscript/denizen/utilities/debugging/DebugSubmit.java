package com.denizenscript.denizen.utilities.debugging;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.debugging.DebugSubmitter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Spigot helper for the core DebugSubmitter.
 */
public class DebugSubmit {

    @Deprecated
    public static List<Supplier<String>> additionalDebugLines = new ArrayList<>();

    public static void init() {
        DebugSubmitter.pasteTitleGetter = () -> "Denizen Debug Logs From " + ChatColor.stripColor(Bukkit.getServer().getMotd());
        DebugSubmitter.debugHeaderLines.add(DebugSubmit::getCoreHeader);
        DebugSubmitter.debugHeaderLines.addAll(additionalDebugLines);
        additionalDebugLines.clear();
    }

    public static String getCoreHeader() {
        if (!DebugSubmitter.debugHeaderLines.isEmpty()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.instance, () -> {
                DebugSubmitter.debugHeaderLines.addAll(additionalDebugLines);
                additionalDebugLines.clear();
            }, 0);
        }
        try {
            // Build a list of plugins
            StringBuilder pluginlist = new StringBuilder();
            int newlineLength = 0;
            int pluginCount = Bukkit.getPluginManager().getPlugins().length;
            for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
                String temp = ((char) 0x01) + (pl.isEnabled() ? "2" : "4") + pl.getName() + ": " + pl.getDescription().getVersion() + ", ";
                pluginlist.append(temp);
                newlineLength += temp.length();
                if (newlineLength > 80) {
                    newlineLength = 0;
                    pluginlist.append("\n");
                }
            }
            // Build a list of worlds
            StringBuilder worldlist = new StringBuilder();
            newlineLength = 0;
            int worldCount = Bukkit.getWorlds().size();
            for (World w : Bukkit.getWorlds()) {
                String temp = w.getName() + ", ";
                worldlist.append(temp);
                newlineLength += temp.length();
                if (newlineLength > 80) {
                    newlineLength = 0;
                    worldlist.append("\n");
                }
            }
            // Build a list of players
            StringBuilder playerlist = new StringBuilder();
            newlineLength = 0;
            int playerCount = Bukkit.getOnlinePlayers().size();
            for (Player pla : Bukkit.getOnlinePlayers()) {
                String temp = pla.getDisplayName() + ChatColor.GRAY + "(" + pla.getName() + "), ";
                playerlist.append(temp);
                newlineLength += temp.length();
                if (newlineLength > 80) {
                    newlineLength = 0;
                    playerlist.append("\n");
                }
            }
            // Prevent errors if the debug was submitted by the server
            if (playerlist.length() < 2) {
                playerlist.append("No Online Players, ");
            }
            int plNormal = 0, plNull = 0, pl3 = 0, pl0 = 0, plWeird = 0;
            try {
                for (UUID id : PlayerTag.getAllPlayers().values()) {
                    if (id == null) {
                        plNull++;
                    }
                    else if (id.version() == 4) {
                        plNormal++;
                    }
                    else if (id.version() == 3) {
                        pl3++;
                    }
                    else if (id.version() == 0) {
                        pl0++;
                    }
                    else {
                        plWeird++;
                    }
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
            String playerSet = (plNormal > 0 ? plNormal + " normal, " : "") + (plNull > 0 ? plNull + " null, " : "")
                    + (pl3 > 0 ? pl3 + " v3, " : "") + (pl0 > 0 ? pl0 + " v0, " : "") + (plWeird > 0 ? plWeird + " other, " : "");
            if (playerSet.length() > 2) {
                playerSet = playerSet.substring(0, playerSet.length() - 2);
            }
            // Gather other setting info
            boolean proxied = false;
            String modeSuffix = "";
            if (Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord")) {
                modeSuffix = " (BungeeCord)";
                proxied = true;
            }
            else if (Denizen.supportsPaper) {
                String paperMode = getPaperOnlineMode();
                if (paperMode != null) {
                    modeSuffix = paperMode;
                    proxied = true;
                }
            }
            String onlineMode = (Bukkit.getServer().getOnlineMode() ? ChatColor.GREEN + "online" : (proxied ? ChatColor.YELLOW : ChatColor.RED) + "offline") + modeSuffix;
            return "Server Version: " + Bukkit.getServer().getName() + " version " + Bukkit.getServer().getVersion()
                    + "\nActive Plugins (" + pluginCount + "): " + pluginlist.substring(0, pluginlist.length() - 2)
                    + "\nLoaded Worlds (" + worldCount + "): " + worldlist.substring(0, worldlist.length() - 2)
                    + "\nOnline Players (" + playerCount + "): " + playerlist.substring(0, playerlist.length() - 2)
                    + "\nTotal Players Ever: " + PlayerTag.getAllPlayers().size() + " (" + playerSet + ")"
                    + "\nMode: " + onlineMode;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return "(Error Building Header)";
        }
    }

    public static YamlConfiguration paperConfig;

    public static String getPaperOnlineMode() {
        boolean isEnabled, isOnline;
        try {
            Class config = Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            Object instance = ReflectionHelper.getFieldValue(config, "instance", null);
            Object proxies = ReflectionHelper.getFieldValue(config, "proxies", instance);
            Object velocity = ReflectionHelper.getFieldValue(proxies.getClass(), "velocity", proxies);
            Field velField = ReflectionHelper.getFields(velocity.getClass()).get("enabled");
            velField.setAccessible(true);
            isEnabled = velField.getBoolean(velocity);
            Field onlineField = ReflectionHelper.getFields(velocity.getClass()).get("onlineMode");
            onlineField.setAccessible(true);
            isOnline = onlineField.getBoolean(velocity);
        }
        catch (ClassNotFoundException ignore) {
            isEnabled = getPaperConfigKey("settings.velocity-support.enabled");
            isOnline = getPaperConfigKey("settings.velocity-support.online-mode");
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
        if (isEnabled) {
            return isOnline ? ChatColor.GREEN + " (Velocity: online)" : ChatColor.RED + " (Velocity: offline)";
        }
        return null;
    }

    public static boolean getPaperConfigKey(String key) {
        if (!Denizen.supportsPaper) {
            return false;
        }
        try {
            if (paperConfig == null) {
                paperConfig = (YamlConfiguration) ReflectionHelper.getFields(Class.forName("com.destroystokyo.paper.PaperConfig")).get("config").get(null);
            }
            return paperConfig.getBoolean(key);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return false;
    }
}
