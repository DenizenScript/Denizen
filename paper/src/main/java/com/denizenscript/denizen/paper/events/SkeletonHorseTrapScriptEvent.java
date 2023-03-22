package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkeletonHorseTrapScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // skeleton horse trap
    //
    // @Location true
    //
    // @Group Paper
    //
    // @Plugin Paper
    //
    // @Triggers when a player gets too close to a trapped skeleton horse and triggers the trap.
    //
    // @Context
    // <context.entity> returns an EntityTag of the skeleton horse.
    // <context.players> returns a ListTag(PlayerTag) of the players involved in the trap.
    // -->

    public SkeletonHorseTrapScriptEvent() {
        registerCouldMatcher("skeleton horse trap");
    }

    public EntityTag entity;
    public SkeletonHorseTrapEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "players" -> {
                ListTag players = new ListTag();
                for (HumanEntity human : event.getEligibleHumans()) {
                    if (!EntityTag.isNPC(human) && human instanceof Player player) {
                        players.addObject(new PlayerTag(player));
                    }
                }
                yield players;
            }
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onSkeletonHorseTrap(SkeletonHorseTrapEvent event) {
        this.event = event;
        entity = new EntityTag(event.getEntity());
        fire(event);
    }
}
