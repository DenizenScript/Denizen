package com.denizenscript.denizen.utilities.command.scripted;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;

public class DenizenAliasHelpTopic extends HelpTopic {

    private final String aliasFor;
    private final HelpMap helpMap;

    public DenizenAliasHelpTopic(String alias, String aliasFor, HelpMap helpMap) {
        this.aliasFor = aliasFor.startsWith("/") ? aliasFor : "/" + aliasFor;
        this.helpMap = helpMap;
        this.name = alias.startsWith("/") ? alias : "/" + alias;
        if (name.equals(aliasFor)) {
            throw new IllegalArgumentException("Command " + this.name + " cannot be alias for itself");
        }
        this.shortText = ChatColor.YELLOW + "Alias for " + ChatColor.WHITE + this.aliasFor;
    }

    public String getFullText(CommandSender forWho) {
        StringBuilder sb = new StringBuilder(this.shortText);
        HelpTopic aliasForTopic = this.helpMap.getHelpTopic(this.aliasFor);
        if (aliasForTopic != null) {
            sb.append("\n");
            sb.append(aliasForTopic.getFullText(forWho));
        }

        return sb.toString();
    }

    public boolean canSee(CommandSender commandSender) {
        if (this.amendedPermission == null) {
            HelpTopic aliasForTopic = this.helpMap.getHelpTopic(this.aliasFor);
            return aliasForTopic != null && aliasForTopic.canSee(commandSender);
        }
        else {
            return commandSender.hasPermission(this.amendedPermission);
        }
    }
}
