package net.aufdemrand.denizen.events;

import net.aufdemrand.denizen.scripts.ScriptEntry;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class ScriptEntryExecuteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private ScriptEntry scriptEntry;

    private boolean cancelled = false;
    private boolean altered = false;
    
    public ScriptEntryExecuteEvent(ScriptEntry scriptEntry) {
        this.scriptEntry = scriptEntry;
    }
 
    public void alterScriptEntry(ScriptEntry scriptEntry) {
        this.scriptEntry = scriptEntry;
        altered = true;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ScriptEntry getScriptEntry() {
        return scriptEntry;
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
