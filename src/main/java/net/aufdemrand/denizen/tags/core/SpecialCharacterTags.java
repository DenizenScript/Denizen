package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpecialCharacterTags implements Listener {

    public SpecialCharacterTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

   @EventHandler
    public void specialCharacterTags(ReplaceableTagEvent event) {
    if (!event.getName().startsWith("&")) return;

       if (event.getName().equalsIgnoreCase("&cm"))
           event.setReplaced(",");

       else if (event.getName().equalsIgnoreCase("&ss"))
           event.setReplaced("§");

       else if (event.getName().equalsIgnoreCase("&sq"))
           event.setReplaced("'");

       else if (event.getName().equalsIgnoreCase("&dq"))
           event.setReplaced("\"");

       else if (event.getName().equalsIgnoreCase("&co"))
           event.setReplaced(":");

       else if (event.getName().equalsIgnoreCase("&rb"))
           event.setReplaced("]");

       else if (event.getName().equalsIgnoreCase("&lb"))
           event.setReplaced("[");

       else if (event.getName().equalsIgnoreCase("&rc"))
           event.setReplaced("}");

       else if (event.getName().equalsIgnoreCase("&lc"))
           event.setReplaced("{");

   }
    
}