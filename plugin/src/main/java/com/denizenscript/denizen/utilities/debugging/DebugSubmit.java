package com.denizenscript.denizen.utilities.debugging;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DebugSubmit extends Thread {

    public String recording;
    public String result = null;

    public String prefix;

    public void build() {
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
            int validPl = 0, invalidPl = 0;
            try {
                for (UUID id : PlayerTag.getAllPlayers().values()) {
                    if (id != null && id.version() == 4) {
                        validPl++;
                    }
                    else {
                        invalidPl++;
                    }
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
            // Gather other setting info
            boolean proxied = false;
            String modeSuffix = "";
            if (Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord")) {
                modeSuffix = " (BungeeCord)";
                proxied = true;
            }
            else if (Denizen.supportsPaper) {
                if (getPaperConfigKey("settings.velocity-support.enabled")) {
                    boolean velocityOnline = getPaperConfigKey("settings.velocity-support.online-mode");
                    modeSuffix = velocityOnline ? ChatColor.GREEN + " (Velocity: online)" : ChatColor.RED + " (Velocity: offline)";
                    proxied = true;
                }
            }
            String onlineMode = (Bukkit.getServer().getOnlineMode() ? ChatColor.GREEN + "online" : (proxied ? ChatColor.YELLOW : ChatColor.RED) + "offline") + modeSuffix;
            prefix = "pastetype=log"
                    + "&response=micro&v=200&pastetitle=Denizen+Debug+Logs+From+" + URLEncoder.encode(ChatColor.stripColor(Bukkit.getServer().getMotd()))
                    + "&pastecontents=" + URLEncoder.encode("Java Version: " + System.getProperty("java.version")
                    + "\nUp-time: " + new DurationTag((CoreUtilities.monotonicMillis() - DenizenCore.startTime) / 1000.0).formatted(false)
                    + "\nServer Version: " + Bukkit.getServer().getName() + " version " + Bukkit.getServer().getVersion()
                    + "\nDenizen Version: Core: " + DenizenCore.VERSION + ", CraftBukkit: " + Denizen.getInstance().coreImplementation.getImplementationVersion()
                    + "\nActive Plugins (" + pluginCount + "): " + pluginlist.substring(0, pluginlist.length() - 2)
                    + "\nLoaded Worlds (" + worldCount + "): " + worldlist.substring(0, worldlist.length() - 2)
                    + "\nOnline Players (" + playerCount + "): " + playerlist.substring(0, playerlist.length() - 2)
                    + "\nTotal Players Ever: " + PlayerTag.getAllPlayers().size() + " (" + validPl + " valid, " + invalidPl + " invalid)"
                    + "\nMode: " + onlineMode
                    + "\nLast reload: " + new DurationTag((CoreUtilities.monotonicMillis() - DenizenCore.lastReloadTime) / 1000.0).formatted(false) + " ago"
                    + "\n\n", "UTF-8");
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static YamlConfiguration paperConfig;

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

    @Override
    public void run() {
        BufferedReader in = null;
        try {
            // Open a connection to the paste server
            URL url = new URL("https://paste.denizenscript.com/New/Log");
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoInput(true);
            uc.setDoOutput(true);
            uc.setConnectTimeout(10000);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            uc.connect();
            // Safely connected at this point
            // Create the final message pack and upload it
            uc.getOutputStream().write((prefix + recording).getBytes(StandardCharsets.UTF_8));
            // Wait for a response from the server
            in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            // Record the response
            result = in.readLine();
            if (result != null && result.startsWith(("<!DOCTYPE html"))) {
                result = null;
            }
            // Close the connection
            in.close();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
        }
    }
}
