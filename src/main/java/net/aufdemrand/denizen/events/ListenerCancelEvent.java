package net.aufdemrand.denizen.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;


/**
 * Triggers when a Denizen Listener is cancelled.
 *
 * @author Jeremy Schroeder
 *
 */

public class ListenerCancelEvent extends ListenerEvent {

    private static final HandlerList handlers = new HandlerList();

    public ListenerCancelEvent(Player player, String id) {
        super(player, id);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}