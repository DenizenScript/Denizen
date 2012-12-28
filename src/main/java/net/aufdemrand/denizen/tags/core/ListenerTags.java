package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ListenerTags implements Listener {

	public ListenerTags(Denizen denizen) {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}
	
	@EventHandler
    public void flagTag(ReplaceableTagEvent event) {
		
	}

}
