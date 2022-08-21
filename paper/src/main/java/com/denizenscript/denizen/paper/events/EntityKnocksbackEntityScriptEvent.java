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
    // <entity> knocks back <entity>
    //
    // @Location true
    //
    // @Switch with:<item> to only process the event when the item used to cause damage (in the damager's hand) is a specified item.
    //
    // @Plugin Paper
    //
    // @Group Paper
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
        registerCouldMatcher("<entity> knocks back <entity>");
        registerSwitches("with");
    }


    public EntityTag entity;
    public EntityTag hitBy;
    public ItemTag held;
    public EntityKnockbackByEntityEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String attacker = path.eventArgLowerAt(0);
        String target = path.eventArgLowerAt(3);
        if (!hitBy.tryAdvancedMatcher(attacker) || (!entity.tryAdvancedMatcher(target))) {
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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj.canBeType(LocationTag.class)) {
            event.getAcceleration().copy(determinationObj.asType(LocationTag.class, getTagContext(path)).toVector());
            return true;
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
        switch (name) {
            case "entity":
                return entity.getDenizenObject();
            case "damager":
                return hitBy.getDenizenObject();
            case "acceleration":
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
