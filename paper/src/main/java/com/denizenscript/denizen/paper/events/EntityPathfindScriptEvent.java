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
    // entity pathfinds
    // <entity> pathfinds
    //
    // @Regex ^on [^\s]+ pathfinds$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    // @Switch to:<area> to only process the event if the entity is pathfinding into a specified area.
    // @Switch at:<entity> to only process the event when the entity is pathfinding at a specified entity.
    //
    // @Plugin Paper
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
        instance = this;
    }

    public static EntityPathfindScriptEvent instance;

    public EntityTag entity;
    public EntityTag target;
    public EntityPathfindEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("pathfinds")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        if (!runInCheck(path, event.getLoc(), "to")) {
            return false;
        }
        String at = path.switches.get("at");
        if (at != null && !tryEntity(target, at)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "EntityPathfinds";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(
                target != null && target.isPlayer() ? target.getDenizenPlayer() : null,
                target != null && target.isCitizensNPC() ? target.getDenizenNPC() : null);
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
