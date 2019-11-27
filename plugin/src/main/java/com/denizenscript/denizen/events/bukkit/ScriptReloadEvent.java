package com.denizenscript.denizen.events.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event for when Denizen dScripts are reloaded. This fires after scripts are reloaded.
 * Also fires when scripts are loaded for the first time, despite the 're' part of the name.
 */
public class ScriptReloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
