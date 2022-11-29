package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityPathfindScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> pathfinds
    //
    // @Location true
    // @Switch to:<area> to only process the event if the entity is pathfinding into a specified area.
    // @Switch at:<entity> to only process the event when the entity is pathfinding at a specified entity.
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when an entity starts pathfinding towards a location or entity.
    //
    // @Context
    // <context.entity> returns the EntityTag that is pathfinding.
    // <context.location> returns the LocationTag that is being pathfound to.
    // <context.target> returns the EntityTag that is being targeted, if any.
    //
    // @Player when the target entity is a player.
    //
    // @NPC when the target entity is an NPC.
    //
    // -->

    public EntityPathfindScriptEvent() {
        registerCouldMatcher("<entity> pathfinds");
        registerSwitches("to", "at");
    }


    public EntityTag entity;
    public EntityTag target;
    public EntityPathfindEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        if (!runInCheck(path, event.getLoc(), "to")) {
            return false;
        }
        if (!path.tryObjectSwitch("at", target)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(target);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("target") && target != null) {
            return target.getDenizenObject();
        }
        else if (name.equals("location")) {
            return new LocationTag(event.getLoc());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event) {
        this.event = event;
        this.entity = new EntityTag(event.getEntity());
        Entity target = event.getTargetEntity();
        this.target = target != null ? new EntityTag(target) : null;
        fire(event);
    }
}
