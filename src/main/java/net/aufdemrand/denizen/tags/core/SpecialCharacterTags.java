package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpecialCharacterTags implements Listener {

    public SpecialCharacterTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    // Thanks geckon :)
    final String[] code = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","k","l","m","n","o","r"};
    @EventHandler
    public void colorTags(ReplaceableTagEvent event) {
        int i = 0;
        for (ChatColor color : ChatColor.values()) 
        {
            if (i > 22) break;
            if (event.matches(color.name()))
                event.setReplaced(color.toString());
            else if (event.matches("&" + i))
                event.setReplaced(ChatColor.getByChar(code[i]) + "");
        }
    }
    
}