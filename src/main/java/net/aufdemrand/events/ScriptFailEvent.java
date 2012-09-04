package net.aufdemrand.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class ScriptFailEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player thePlayer;
    private String theScript;
    private int count;
 
    public ScriptFailEvent(Player thePlayer, String theScript, int count) {
        this.theScript = theScript;
        this.thePlayer = thePlayer;
        this.count = count;
    }
    
    public int getCount() {
    	return count;
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