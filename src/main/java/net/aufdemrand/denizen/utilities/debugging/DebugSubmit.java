package net.aufdemrand.denizen.utilities.debugging;

import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DebugSubmit extends Thread {
    public CommandSender sender;
    public String recording;
    @Override
    public void run() {
        try {
            URL url = new URL("http://mcmonkey4eva.dyndns.org/paste");
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoInput(true);
            uc.setDoOutput(true);
            uc.setConnectTimeout(10000);
            uc.connect();
            uc.getOutputStream().write(
                ("postid=pastetext&pastetype=log"
                        + "&response=micro&pastetitle=Denizen+Debug+Logs+From+" + URLEncoder.encode(Bukkit.getServer().getMotd().replace(ChatColor.COLOR_CHAR, (char) 0x01))
                        + "&pastecontents=" + recording)
                        .getBytes("UTF-8"));
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            Messaging.send(sender, ChatColor.GREEN + "Successfully submitted to http://mcmonkey4eva.dyndns.org" + in.readLine());
            in.close();
        }
        catch (Exception e) {
            if (dB.showStackTraces) {
                e.printStackTrace();
            }
            Messaging.send(sender, ChatColor.RED + "Error while submitting.");
        }
    }
}
