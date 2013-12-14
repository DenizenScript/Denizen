package net.aufdemrand.denizen.utilities.debugging;

import net.aufdemrand.denizen.utilities.debugging.dB;
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

public class DebugSubmit extends Thread {
    public String recording;
    public String Result = null;
    @Override
    public void run() {
        BufferedReader in = null;
        try {
            // Open a connection to the paste server
            URL url = new URL("http://mcmonkey4eva.dyndns.org/paste");
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoInput(true);
            uc.setDoOutput(true);
            uc.setConnectTimeout(10000);
            uc.connect();
            // Safely connected at this point
            // Build a list of plugins
            StringBuilder pluginlist = new StringBuilder();
            int newlineLength = 0;
            for (Plugin pl: Bukkit.getPluginManager().getPlugins()) {
                String temp = ((char)0x01) + (pl.isEnabled() ? "2": "4") + pl.getName() + ": " + pl.getDescription().getVersion() + ", ";
                pluginlist.append(temp);
                newlineLength += temp.length();
                if (newlineLength > 80) {
                    newlineLength = 0;
                    pluginlist.append("\n");
                }
            }
            // Build a list of worlds
            StringBuilder worldlist = new StringBuilder();
            for (World w: Bukkit.getWorlds()) {
                worldlist.append(w.getName() + ", ");
            }
            // Build a list of players
            StringBuilder playerlist = new StringBuilder();
            newlineLength = 0;
            for (Player pla: Bukkit.getOnlinePlayers()) {
                String temp = pla.getDisplayName().replace(ChatColor.COLOR_CHAR, (char)0x01) + ((char)0x01) + "7(" + pla.getName() + "), ";
                playerlist.append(temp);
                newlineLength += temp.length();
                if (newlineLength > 80) {
                    newlineLength = 0;
                    playerlist.append("\n");
                }
            }
            // Prevent errors if the debug was submitted by the server
            if (playerlist.length() < 2)
                playerlist.append("No Online Players, ");
            // Create the final message pack and upload it
            uc.getOutputStream().write(("postid=pastetext&pastetype=log"
                        + "&response=micro&v=100&pastetitle=Denizen+Debug+Logs+From+" + URLEncoder.encode(Bukkit.getServer().getMotd().replace(ChatColor.COLOR_CHAR, (char) 0x01))
                        + "&pastecontents=" + URLEncoder.encode("CraftBukkit Version: " + Bukkit.getServer().getVersion() + "\nActive Plugins: "
                        + pluginlist.substring(0, pluginlist.length() - 2) + "\nLoaded Worlds: " + worldlist.substring(0, worldlist.length() - 2) + "\nOnline Players: "
                        + playerlist.substring(0, playerlist.length() - 2) + "\n\n") + recording)
                        .getBytes("UTF-8"));
            // Wait for a response from the server
            in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            // Record the response
            Result = in.readLine();
            // Close the connection
            in.close();
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        finally {
            try {
            if (in != null)
                in.close();
            }
            catch (Exception e) {
                dB.echoError(e);
            }
        }
    }
}
