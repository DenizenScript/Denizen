package net.aufdemrand.denizen.events.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * Bukkit event for when a dScript FINISH command is executed.
 *
 * @author Jeremy Schroeder
 */
public class ScriptFinishEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String playerName;
    private String scriptName;
    private int count;

    public ScriptFinishEvent(String playerName, String theScript, int count) {
        this.scriptName = theScript;
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
