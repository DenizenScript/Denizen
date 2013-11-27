package net.aufdemrand.denizen.utilities.debugging;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.CitizensAPI;
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
            URL url = new URL("http://mcmonkey4eva.dyndns.org/paste");
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoInput(true);
            uc.setDoOutput(true);
            uc.setConnectTimeout(10000);
            uc.connect();
            String pluginlist = "";
            int newlineLength = 0;
            for (Plugin pl: Bukkit.getPluginManager().getPlugins()) {
                String temp = ((char)0x01) + (pl.isEnabled() ? "2": "4") + pl.getName() + ": " + pl.getDescription().getVersion() + ", ";
                pluginlist += temp;
                newlineLength += temp.length();
                if (newlineLength > 80) {
                    newlineLength = 0;
                    pluginlist += "\n";
                }
            }
            String worldlist = "";
            for (World w: Bukkit.getWorlds()) {
                worldlist += w.getName() + ", ";
            }
            String playerlist = "";
            newlineLength = 0;
            for (Player pla: Bukkit.getOnlinePlayers()) {
                String temp = pla.getDisplayName().replace(ChatColor.COLOR_CHAR, (char)0x01) + ((char)0x01) + "7(" + pla.getName() + "), ";
                playerlist += temp;
                newlineLength += temp.length();
                if (newlineLength > 80) {
                    newlineLength = 0;
                    playerlist += "\n";
                }
            }
            if (playerlist.length() < 2)
                playerlist = "No Online Players, ";
            uc.getOutputStream().write(("postid=pastetext&pastetype=log"
                        + "&response=micro&pastetitle=Denizen+Debug+Logs+From+" + URLEncoder.encode(Bukkit.getServer().getMotd().replace(ChatColor.COLOR_CHAR, (char) 0x01))
                        + "&pastecontents=" + URLEncoder.encode("CraftBukkit Version: " + Bukkit.getServer().getVersion() + "\nActive Plugins: "
                        + pluginlist.substring(0, pluginlist.length() - 2) + "\nLoaded Worlds: " + worldlist.substring(0, worldlist.length() - 2) + "\nOnline Players: "
                        + playerlist.substring(0, playerlist.length() - 2) + "\n\n") + recording)
                        .getBytes("UTF-8"));
            in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            Result = in.readLine();
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
