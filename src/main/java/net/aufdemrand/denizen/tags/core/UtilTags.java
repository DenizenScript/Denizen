package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class UtilTags implements Listener {

    public UtilTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

   @EventHandler
    public void utilTags(ReplaceableTagEvent event) {
    if (!event.matches("UTIL")) return;

       if (event.getType() == null) return;

       if (event.getType().equalsIgnoreCase("RANDOM_UUID"))
           event.setReplaced(UUID.randomUUID().toString());

   }
    
}