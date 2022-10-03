package com.denizenscript.denizen.utilities.debugging;

import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class DebugConsoleSender {

    public static boolean showColor = true;

    static CommandSender commandSender = null;

    public static void sendMessage(String string) {
        if (commandSender == null) {
            commandSender = Bukkit.getServer().getConsoleSender();
        }
        //                                                                       "[HH:mm:ss INFO]: "
        string = CoreConfiguration.debugPrefix + string.replace("<FORCE_ALIGN>", "                 ");
        if (showColor) {
            PaperAPITools.instance.sendConsoleMessage(commandSender, string);
        }
        else {
            commandSender.sendMessage(ChatColor.stripColor(string));
        }
    }
}
