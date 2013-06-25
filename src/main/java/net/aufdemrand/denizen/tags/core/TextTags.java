package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TextTags implements Listener {

    public TextTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void foreignCharacterTags(ReplaceableTagEvent event) {

        if (!event.getName().startsWith("&")) return;

        if (event.getName().equals("&auml"))
            event.setReplaced("ä");

        else if (event.getName().equals("&Auml"))
            event.setReplaced("Ä");

        else if (event.getName().equals("&ouml"))
            event.setReplaced("ö");

        else if (event.getName().equals("&Ouml"))
            event.setReplaced("Ö");

        else if (event.getName().equals("&uuml"))
            event.setReplaced("ü");

        else if (event.getName().equals("&Uuml"))
            event.setReplaced("Ü");

    }

    // Thanks geckon :)
    final String[] code = {"0","1","2","3","4","5","6","7","8","9"
            ,"a","b","c","d","e","f","k","l","m","n","o","r"};

    @EventHandler
    public void colorTags(ReplaceableTagEvent event) {
        int i = 0;
        for (ChatColor color : ChatColor.values()) {
            if (i > 22) break;
            if (event.matches(color.name()))
                event.setReplaced(color.toString());
            else if (event.matches("&" + code[i]))
                event.setReplaced(ChatColor.getByChar(code[i]).toString());
            i++;
        }
    }
    
}