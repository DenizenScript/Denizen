package net.aufdemrand.denizen.utilities.debugging;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

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
            uc.getOutputStream().write(
                ("postid=pastetext&pastetype=log"
                        + "&response=micro&pastetitle=Denizen+Debug+Logs+From+" + URLEncoder.encode(Bukkit.getServer().getMotd().replace(ChatColor.COLOR_CHAR, (char) 0x01))
                        + "&pastecontents=" + recording)
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
