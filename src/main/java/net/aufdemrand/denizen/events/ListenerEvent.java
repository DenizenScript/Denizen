package net.aufdemrand.denizen.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 *
 *
 * @author Jeremy Schroeder
 *
 */

public class ListenerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String id;

    public ListenerEvent(Player player, String id) {
        this.player = player;
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public String getId() {
        return id;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}