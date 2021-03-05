package com.denizenscript.denizen.utilities.command.manager.messaging;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Colorizer {

    public static String parseColors(String parsed) {
        Matcher matcher = COLOR_MATCHER.matcher(ChatColor.translateAlternateColorCodes('&', parsed));
        return matcher.replaceAll(GROUP);
    }

    public static String stripColors(String parsed) {
        Matcher matcher = COLOR_MATCHER.matcher(parsed);
        return matcher.replaceAll("");
    }

    private static Pattern COLOR_MATCHER;
    private static String GROUP = ChatColor.COLOR_CHAR + "$1";

    static {
        StringBuilder colors = new StringBuilder("<([");
        for (ChatColor color : ChatColor.values()) {
            colors.append(color.getChar());
        }
        colors.append("])>");
        COLOR_MATCHER = Pattern.compile(colors.toString(), Pattern.CASE_INSENSITIVE);
    }
}
