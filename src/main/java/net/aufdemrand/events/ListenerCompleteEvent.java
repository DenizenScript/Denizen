package net.aufdemrand.events;

import net.aufdemrand.denizen.listeners.AbstractListener;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class ListenerCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player thePlayer;
    private String theScript;
	private AbstractListener theListener;
 
    public ListenerCompleteEvent(Player thePlayer, AbstractListener theListener) {
    	this.theListener = theListener;
    	this.thePlayer = thePlayer;
    }
    
    public String getScriptName() {
        return theScript;
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