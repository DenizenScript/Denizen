package com.denizenscript.denizen.utilities.debugging;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
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
    public String Result = null;

    @Override
    public void run() {
        BufferedReader in = null;
        try {
            // Open a connection to the paste server
            URL url = new URL("http://paste.denizenscript.com/New/Log");
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoInput(true);
            uc.setDoOutput(true);
            uc.setConnectTimeout(10000);
            uc.connect();
            // Safely connected at this point
            // Build a list of plugins
            StringBuilder pluginlist = new StringBuilder();
            int newlineLength = 0;
            int pluginCount = Bukkit.getPluginManager().getPlugins().length;
            for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
                String temp = (pl.isEnabled() ? ChatColor.DARK_GREEN : ChatColor.DARK_RED) + pl.getName() + ": " + pl.getDescription().getVersion() + ", ";
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
            boolean bungee = Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord");
            // Create the final message pack and upload it
            uc.getOutputStream().write(("pastetype=log"
                    + "&response=micro&v=200&pastetitle=Denizen+Debug+Logs+From+" + URLEncoder.encode(ChatColor.stripColor(Bukkit.getServer().getMotd()))
                    + "&pastecontents=" + URLEncoder.encode(("Java Version: " + System.getProperty("java.version")
                    + "\nUp-time: " + new DurationTag((System.currentTimeMillis() - Denizen.startTime) / 50).formatted()
                    + "\nServer Version: " + Bukkit.getServer().getName() + " version " + Bukkit.getServer().getVersion()
                    + "\nDenizen Version: Core: " + DenizenCore.VERSION + ", CraftBukkit: " + Denizen.getInstance().coreImplementation.getImplementationVersion()
                    + "\nActive Plugins (" + pluginCount + "): " + pluginlist.substring(0, pluginlist.length() - 2)
                    + "\nLoaded Worlds (" + worldCount + "): " + worldlist.substring(0, worldlist.length() - 2)
                    + "\nOnline Players (" + playerCount + "): " + playerlist.substring(0, playerlist.length() - 2)
                    + "\nTotal Players Ever: " + PlayerTag.getAllPlayers().size() + " (" + validPl + " valid, " + invalidPl + " invalid)"
                    + "\nMode: " + (Bukkit.getServer().getOnlineMode() ? ChatColor.GREEN + "online" : (bungee ? ChatColor.YELLOW : ChatColor.RED) + "offline") + (bungee ? " (BungeeCord)" : "")
                    + "\n\n")) + recording)
                    .getBytes(StandardCharsets.UTF_8));
            // Wait for a response from the server
            in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            // Record the response
            Result = in.readLine();
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
