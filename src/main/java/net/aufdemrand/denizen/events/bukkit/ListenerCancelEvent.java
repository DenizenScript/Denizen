package net.aufdemrand.denizen.events.bukkit;

import net.aufdemrand.denizen.objects.dPlayer;
import org.bukkit.event.HandlerList;


/**
 * Triggers when a Player Listener is cancelled.
 *
 * @author Jeremy Schroeder
 */

public class ListenerCancelEvent extends ListenerEvent {

    private static final HandlerList handlers = new HandlerList();

    public ListenerCancelEvent(dPlayer player, String id) {
        super(player, id);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
