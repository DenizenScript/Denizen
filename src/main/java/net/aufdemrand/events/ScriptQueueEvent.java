package net.aufdemrand.events;

import net.aufdemrand.denizen.scripts.ScriptEntry;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class ScriptQueueEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player thePlayer;
    private String theScript;
    private String theCommand;
    private ScriptEntry scriptEntry;
    boolean cancelled = false;
    boolean altered = false;
    
    public ScriptQueueEvent(Player thePlayer, ScriptEntry scriptEntry) {
        this.theScript = scriptEntry.getScript();
        this.thePlayer = thePlayer;
        this.theCommand = scriptEntry.getCommand();
        this.scriptEntry = scriptEntry;
    }
 
    public boolean isAltered() {
    	return altered;
    }
    
    public void alterScriptEntry(ScriptEntry scriptEntry) {
    	this.scriptEntry = scriptEntry;
    	altered = true;
    }
    
    public String getCommand() {
    	return theCommand;
    }
    
    public ScriptEntry getScriptEntry() {
    	return scriptEntry;
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		cancelled = arg0;
		
	}
}