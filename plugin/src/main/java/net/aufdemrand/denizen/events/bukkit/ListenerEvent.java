package net.aufdemrand.denizen.events.bukkit;

import net.aufdemrand.denizen.objects.dPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * An event that pertains to a Denizen 'Player Listener'.
 *
 * @author Jeremy Schroeder
 */

public class ListenerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private dPlayer player;
    private String id;

    public ListenerEvent(dPlayer player, String id) {
        this.player = player;
        this.id = id;
    }

    public dPlayer getPlayer() {
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
