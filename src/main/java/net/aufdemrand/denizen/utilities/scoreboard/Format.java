package net.aufdemrand.denizen.utilities.scoreboard;

import org.bukkit.ChatColor;

public class Format {
	
	//Format: this is a kind of a string replacer.
	//it replaces the color-codes (with on a old method)
	
    private final ScoreboardAPI plugin;
    
	//constructor - init plugin
    public Format(final ScoreboardAPI plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        this.plugin = plugin;
    }
    
	//replaces all color-codes with colors
	public String replaceCC(String text) {
		text = text.replaceAll("&0", ChatColor.BLACK + "");
		text = text.replaceAll("&1", ChatColor.DARK_BLUE + "");
		text = text.replaceAll("&2", ChatColor.DARK_GREEN + "");
		text = text.replaceAll("&3", ChatColor.DARK_AQUA + "");
		text = text.replaceAll("&4", ChatColor.DARK_RED + "");
		text = text.replaceAll("&5", ChatColor.DARK_PURPLE + "");
		text = text.replaceAll("&6", ChatColor.GOLD + "");
		text = text.replaceAll("&7", ChatColor.GRAY + "");
		text = text.replaceAll("&8", ChatColor.DARK_GRAY + "");
		text = text.replaceAll("&9", ChatColor.BLUE + "");
		text = text.replaceAll("&a", ChatColor.GREEN + "");
		text = text.replaceAll("&b", ChatColor.AQUA + "");
		text = text.replaceAll("&c", ChatColor.RED + "");
		text = text.replaceAll("&d", ChatColor.LIGHT_PURPLE + "");
		text = text.replaceAll("&e", ChatColor.YELLOW + "");
		text = text.replaceAll("&f", ChatColor.WHITE + "");
		text = text.replaceAll("&k", ChatColor.MAGIC + "");
		text = text.replaceAll("&l", ChatColor.BOLD + "");
		text = text.replaceAll("&m", ChatColor.STRIKETHROUGH + "");
		text = text.replaceAll("&n", ChatColor.UNDERLINE + "");
		text = text.replaceAll("&o", ChatColor.ITALIC + "");
		text = text.replaceAll("&r", ChatColor.RESET + "");
		return text;
	}

	//removes all color-codes (for console-messages)
	public String removeCC(String text) {
        text = text.replaceAll("&0", "");
        text = text.replaceAll("&1", "");
        text = text.replaceAll("&2", "");
        text = text.replaceAll("&3", "");
        text = text.replaceAll("&4", "");
        text = text.replaceAll("&5", "");
        text = text.replaceAll("&6", "");
        text = text.replaceAll("&7", "");
        text = text.replaceAll("&8", "");
        text = text.replaceAll("&9", "");
        text = text.replaceAll("&a", "");
        text = text.replaceAll("&b", "");
        text = text.replaceAll("&c", "");
        text = text.replaceAll("&d", "");
        text = text.replaceAll("&e", "");
        text = text.replaceAll("&f", "");
        text = text.replaceAll("&k", "");
        text = text.replaceAll("&l", "");
        text = text.replaceAll("&m", "");
        text = text.replaceAll("&n", "");
        text = text.replaceAll("&o", "");
        text = text.replaceAll("&r", "");
        
        text = text.replaceAll("§0", "");
        text = text.replaceAll("§1", "");
        text = text.replaceAll("§2", "");
        text = text.replaceAll("§3", "");
        text = text.replaceAll("§4", "");
        text = text.replaceAll("§5", "");
        text = text.replaceAll("§6", "");
        text = text.replaceAll("§7", "");
        text = text.replaceAll("§8", "");
        text = text.replaceAll("§9", "");
        text = text.replaceAll("§a", "");
        text = text.replaceAll("§b", "");
        text = text.replaceAll("§c", "");
        text = text.replaceAll("§d", "");
        text = text.replaceAll("§e", "");
        text = text.replaceAll("§f", "");
        text = text.replaceAll("§k", "");
        text = text.replaceAll("§l", "");
        text = text.replaceAll("§m", "");
        text = text.replaceAll("§n", "");
        text = text.replaceAll("§o", "");
        text = text.replaceAll("§r", "");

        text = text.replaceAll(ChatColor.AQUA + "", "");
        text = text.replaceAll(ChatColor.BLACK + "", "");
        text = text.replaceAll(ChatColor.BLUE + "", "");
        text = text.replaceAll(ChatColor.BOLD + "", "");
        text = text.replaceAll(ChatColor.DARK_AQUA + "", "");
        text = text.replaceAll(ChatColor.DARK_BLUE + "", "");
        text = text.replaceAll(ChatColor.DARK_GRAY + "", "");
        text = text.replaceAll(ChatColor.DARK_GREEN + "", "");
        text = text.replaceAll(ChatColor.DARK_PURPLE + "", "");
        text = text.replaceAll(ChatColor.DARK_RED + "", "");
        text = text.replaceAll(ChatColor.GOLD + "", "");
        text = text.replaceAll(ChatColor.GRAY + "", "");
        text = text.replaceAll(ChatColor.GREEN + "", "");
        text = text.replaceAll(ChatColor.ITALIC + "", "");
        text = text.replaceAll(ChatColor.LIGHT_PURPLE + "", "");
        text = text.replaceAll(ChatColor.MAGIC + "", "");
        text = text.replaceAll(ChatColor.RED + "", "");
        text = text.replaceAll(ChatColor.RESET + "", "");
        text = text.replaceAll(ChatColor.STRIKETHROUGH + "", "");
        text = text.replaceAll(ChatColor.UNDERLINE + "", "");
        text = text.replaceAll(ChatColor.WHITE + "", "");
        text = text.replaceAll(ChatColor.YELLOW + "", "");
		return text;
	}
	
	//here we can add other replacer's like &player to player
	
}
