package net.aufdemrand.denizen.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;


/**
 * An event that fires on a Player finishing a Listener.
 *
 * @author Jeremy Schroeder
 *
 */

public class ListenerFinishEvent extends ListenerEvent {

    private static final HandlerList handlers = new HandlerList();

    public ListenerFinishEvent(Player player, String id) {
        super(player, id);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}