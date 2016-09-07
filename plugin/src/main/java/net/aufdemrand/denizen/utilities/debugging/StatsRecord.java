package net.aufdemrand.denizen.utilities.debugging;

import net.aufdemrand.denizen.scripts.containers.core.VersionScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.tags.core.EscapeTags;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class StatsRecord extends Thread {
    @Override
    public void run() {
        BufferedReader in = null;
        try {
            // Open a connection to the stats server
            URL url = new URL("http://neo.mcmonkey.org/plugins/public_stats?plugin=Denizen&version=" + URLEncoder.encode(DenizenAPI.getCurrentInstance().getImplementationVersion()));
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoInput(true);
            uc.setDoOutput(true);
            uc.setConnectTimeout(10000);
            uc.connect();
            // Safely connected at this point
            // Get some general info
            StringBuilder sb = new StringBuilder();
            for (VersionScriptContainer cont : VersionScriptContainer.scripts) {
                sb.append(EscapeTags.Escape(cont.getString("ID"))).append("|");
            }
            // Create the final message pack and upload it
            uc.getOutputStream().write(("postid=pluginstats&plugin_st_players=" + Bukkit.getOnlinePlayers().size()
                    + "&plugin_st_motd=" + URLEncoder.encode(Bukkit.getServer().getMotd().replace(ChatColor.COLOR_CHAR, (char) 0x01))
                    + "&plugin_st_known_scripts=" + sb.toString())
                    .getBytes("UTF-8"));
            // Wait for a response from the server
            in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            // Record the response
            String Result = in.readLine();
            // TODO: Use return?
            // Close the connection
            in.close();
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (Exception e) {
                dB.echoError(e);
            }
        }
    }
}
