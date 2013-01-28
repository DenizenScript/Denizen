package net.aufdemrand.denizen.events;

import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.arguments.Script;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class ScriptEntryExecuteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private Script script;
    private String commandName;
    private ScriptEntry scriptEntry;

    private boolean cancelled = false;
    private boolean altered = false;
    
    public ScriptEntryExecuteEvent(Player player, ScriptEntry scriptEntry) {
        this.script = scriptEntry.getScript();
        this.player = player;
        this.commandName = scriptEntry.getCommand();
        this.scriptEntry = scriptEntry;
    }
 
    public void alterScriptEntry(ScriptEntry scriptEntry) {
    	this.scriptEntry = scriptEntry;
    	altered = true;
    }

    public String getCommand() {
    	return commandName;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
    	return player;
    }

    public ScriptEntry getScriptEntry() {
    	return scriptEntry;
    }
    
    public Script getScript() {
        return script;
    }
    
    public boolean isAltered() {
    	return altered;
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