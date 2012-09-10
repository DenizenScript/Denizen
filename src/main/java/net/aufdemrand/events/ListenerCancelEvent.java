package net.aufdemrand.events;

import net.aufdemrand.denizen.listeners.AbstractListener;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class ListenerCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player thePlayer;
    private String listenerId;
 
    public ListenerCancelEvent(Player thePlayer, String listenerId) {
    	this.thePlayer = thePlayer;
    	this.listenerId = listenerId;
    }
    
    public String getListenerId() {
        return listenerId;
    }
    
    public Player getPlayer() {
    	return thePlayer;
    }
 
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}