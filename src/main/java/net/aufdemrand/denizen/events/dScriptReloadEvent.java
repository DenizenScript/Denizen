package net.aufdemrand.denizen.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 

/**
 * Bukkit event for when scripts are reloaded.
 * 
 * @author Jeremy Schroeder
 *
 */

public class dScriptReloadEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
 
    public ScriptsReloadEvent() {
    	// Nothing to do here.
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}