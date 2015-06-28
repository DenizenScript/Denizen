package net.aufdemrand.denizen.events.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * Bukkit event for when a dScript FAIL command is executed.
 *
 * @author Jeremy Schroeder
 */

public class ScriptFailEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private String playerName;
    private String scriptName;
    private int count;

    public ScriptFailEvent(String playerName, String scriptName, int count) {
        this.scriptName = scriptName;
        this.playerName = playerName;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public String getScriptName() {
        return scriptName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
