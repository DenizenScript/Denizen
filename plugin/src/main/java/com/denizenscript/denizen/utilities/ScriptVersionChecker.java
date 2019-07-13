package com.denizenscript.denizen.utilities;


import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.scripts.containers.core.VersionScriptContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScriptVersionChecker {

    public final VersionScriptContainer container;

    public ScriptVersionChecker(VersionScriptContainer vers) {
        container = vers;
    }

    public void runme(final CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    final String ID = container.getString("ID");
                    URL url = new URL("http://one.denizenscript.com/denizen/repo/version/" + ID);
                    HttpURLConnection uc = (HttpURLConnection) url.openConnection();
                    uc.setDoInput(true);
                    uc.setDoOutput(false);
                    uc.setConnectTimeout(10000);
                    uc.connect();
                    BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                    final String Result = in.readLine();
                    in.close();
                    Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                        @Override
                        public void run() {
                            String vers = container.getString("VERSION");
                            String name = container.getString("NAME");
                            if (Result.equalsIgnoreCase(vers)) {
                                sender.sendMessage(ChatColor.AQUA + name + ChatColor.GREEN + " is up to date!");
                            }
                            else {
                                sender.sendMessage(ChatColor.AQUA + name + ChatColor.RED + " is NOT up to date!");
                                sender.sendMessage(ChatColor.RED + "Your version: " + vers + ", repo version: " + Result);
                                sender.sendMessage(ChatColor.RED + "Update at: http://one.denizenscript.com/denizen/repo/entry/" + ID);
                            }
                        }
                    });
                }
                catch (final Throwable ex) {
                    Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                        @Override
                        public void run() {
                            Debug.echoError(ex);
                        }
                    });
                }
            }
        });
    }
}
