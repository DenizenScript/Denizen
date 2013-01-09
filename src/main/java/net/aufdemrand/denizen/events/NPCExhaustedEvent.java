package net.aufdemrand.denizen.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event for when Denizen dScripts are reloaded.
 * 
 * @author Jeremy Schroeder
 *
 */
public class NPCExhaustedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public NPCExhaustedEvent() {
    	// Nothing to do here.
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}