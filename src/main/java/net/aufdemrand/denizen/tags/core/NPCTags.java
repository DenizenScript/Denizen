package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;

import org.bukkit.event.Listener;

public class NPCTags implements Listener {

    public NPCTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }


    
}