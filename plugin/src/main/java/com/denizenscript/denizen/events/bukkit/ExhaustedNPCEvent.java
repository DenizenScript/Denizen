package com.denizenscript.denizen.events.bukkit;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event for when an NPC is exhausted. Exhausted NPCs cannot move
 * until their hunger level is below maxhunger, as tracked by the HungerTrait
 * provided by Denizen.
 */
public class ExhaustedNPCEvent extends NPCEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public ExhaustedNPCEvent(NPC npc) {
        super(npc);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
