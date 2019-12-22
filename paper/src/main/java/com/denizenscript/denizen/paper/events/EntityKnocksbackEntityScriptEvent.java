package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityKnocksbackEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity knocks back entity
    // entity knocks back <entity>
    // <entity> knocks back entity
    // <entity> knocks back <entity>
    //
    // @Regex ^on [^\s]+ knocks back [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    // @Switch with:<item> to only process the event when the item used to cause damage (in the damager's hand) is a specified item.
    //
    // @Plugin Paper
    //
    // @Cancellable true
    //
    // @Triggers when an entity is knocked back from the hit of another entity.
    //
    // @Context
    // <context.entity> returns the EntityTag that was knocked back.
    // <context.damager> returns the EntityTag of the one who knocked.
    // <context.acceleration> returns the knockback applied as a vector.
    //
    // @Determine
    // LocationTag as a vector to change the acceleration applied.
    //
    // @Player when the damager or damaged entity is a player. Cannot be both.
    //
    // @NPC when the damager or damaged entity is an NPC. Cannot be both.
    //
    // -->

    public EntityKnocksbackEntityScriptEvent() {
        instance = this;
    }

    public static EntityKnocksbackEntityScriptEvent instance;

    public EntityTag entity;
    public EntityTag hitBy;
    public ItemTag held;
    public EntityKnockbackByEntityEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(1).equals("knocks") &&
                path.eventArgLowerAt(2).equals("back");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String attacker = path.eventArgLowerAt(0);
        String target = path.eventArgLowerAt(3);
        if (!tryEntity(hitBy, attacker) || (!tryEntity(entity, target))) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        if (!runWithCheck(path, held)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "EntityKnocksbackEntity";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (!isDefaultDetermination(determinationObj)) {
            String determination = determinationObj.toString();
            if (LocationTag.matches(determination)) {
                event.getAcceleration().copy((LocationTag.valueOf(determination)).toVector());
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(
                hitBy.isPlayer() ? hitBy.getDenizenPlayer() : entity.isPlayer() ? entity.getDenizenPlayer() : null,
                hitBy.isCitizensNPC() ? hitBy.getDenizenNPC() : entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("damager")) {
            return hitBy.getDenizenObject();
        }
        else if (name.equals("acceleration")) {
            return new LocationTag(event.getAcceleration());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityKnockbackEntity(EntityKnockbackByEntityEvent event) {
        entity = new EntityTag(event.getEntity());
        hitBy = new EntityTag(event.getHitBy());
        held = hitBy.getItemInHand();
        this.event = event;
        fire(event);
    }
}
