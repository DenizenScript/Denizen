package com.denizenscript.denizen.events.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event for when Denizen 'saves' are reloaded.
 */
public class SavesReloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
