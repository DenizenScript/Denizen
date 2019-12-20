package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class EntityKnockbackByEntityScriptEvent extends BukkitScriptEvent implements Listener  {

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
    //
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
    // <context.attacker> returns the EntityTag of the one who knocked.
    // <context.acceleration> returns the knockback applied as a vector.
    //
    // @Player when the damager or damaged entity is a player. Cannot be both.
    //
    // @NPC when the damager or damaged entity is an NPC. Cannot be both.
    //
    // -->

    public EntityKnockbackByEntityScriptEvent() {
        instance = this;
    }

    public static EntityKnockbackByEntityScriptEvent instance;

    public EntityTag entity;
    public EntityTag hitBy;
    public ItemTag held;
    public LocationTag acceleration;
    public EntityKnockbackByEntityEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
         return CoreUtilities.getXthArg(1, lower).equals("knocks") &&
                CoreUtilities.getXthArg(2, lower).equals("back");
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
        return "EntityKnocksbackEntityEvent";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof LocationTag) {
            acceleration = new LocationTag(((LocationTag) determinationObj).toVector());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(
                hitBy.isPlayer() ? hitBy.getDenizenPlayer() :
                        (entity.isPlayer() ? entity.getDenizenPlayer() : null),

                hitBy.isCitizensNPC() ? hitBy.getDenizenNPC() :
                        (entity.isCitizensNPC() ? EntityTag.getNPCFrom(event.getHitBy()) : null)
        );
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("attacker")) {
            return hitBy.getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityKnockbackEntity(EntityKnockbackByEntityEvent event) {
        entity = new EntityTag(event.getEntity());
        hitBy = new EntityTag(event.getHitBy());
        acceleration = new LocationTag(event.getAcceleration());
        held = hitBy.getItemInHand();
        this.event = event;
        fire(event);
    }
}
