package com.denizenscript.denizen.utilities.command.manager.messaging;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messaging {

    public static void configure(String messageColour, String highlightColour) {
        MESSAGE_COLOUR = messageColour;
        HIGHLIGHT_COLOUR = highlightColour;
    }

    private static String prettify(String message) {
        String trimmed = message.trim();
        String messageColor = Colorizer.parseColors(MESSAGE_COLOUR);
        if (!trimmed.isEmpty()) {
            if (trimmed.charAt(0) == ChatColor.COLOR_CHAR) {
                ChatColor test = ChatColor.getByChar(trimmed.substring(1, 2));
                if (test == null) {
                    message = messageColor + message;
                }
            }
            else {
                message = messageColor + message;
            }
        }
        return message;
    }

    public static void send(CommandSender sender, String msg) {
        sendMessageTo(sender, msg);
    }

    public static void sendInfo(CommandSender sender, String msg) {
        send(sender, ChatColor.YELLOW + msg);
    }

    public static void sendError(CommandSender sender, String msg) {
        send(sender, ChatColor.RED + msg);
    }

    private static void sendMessageTo(CommandSender sender, String rawMessage) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            rawMessage = rawMessage.replace("<player>", player.getName());
            rawMessage = rawMessage.replace("<world>", player.getWorld().getName());
        }
        rawMessage = Colorizer.parseColors(rawMessage);
        for (String message : rawMessage.split("<br>|<n>|\\n")) {
            sender.sendMessage(prettify(message));
        }
    }

    private static String HIGHLIGHT_COLOUR = ChatColor.YELLOW.toString();

    private static String MESSAGE_COLOUR = ChatColor.GREEN.toString();
}
