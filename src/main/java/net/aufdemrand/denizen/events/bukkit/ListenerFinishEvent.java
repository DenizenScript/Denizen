package net.aufdemrand.denizen.events.bukkit;

import net.aufdemrand.denizen.objects.dPlayer;
import org.bukkit.event.HandlerList;


/**
 * An event that fires on a Player finishing a ' Player Listener'.
 *
 * @author Jeremy Schroeder
 */

public class ListenerFinishEvent extends ListenerEvent {

    private static final HandlerList handlers = new HandlerList();

    public ListenerFinishEvent(dPlayer player, String id) {
        super(player, id);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
