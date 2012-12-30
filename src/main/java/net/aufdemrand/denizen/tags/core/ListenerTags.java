package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ListenerTags implements Listener {

	public ListenerTags(Denizen denizen) {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}
	
	/*
	 * <@aufdemrand> LISTENER is event.getName()
	 * <@aufdemrand> listener_ID is event.getType()
	 * <@aufdemrand> and variable_name is event.getValue()
	 * <@aufdemrand> fallback data is valid in all tags too
	 * <@aufdemrand> <LISTENER.TEST:VAR(Fallback text.)>
	 */
	
	@EventHandler
    public void listenTag(ReplaceableTagEvent event) {
		
	}

}
