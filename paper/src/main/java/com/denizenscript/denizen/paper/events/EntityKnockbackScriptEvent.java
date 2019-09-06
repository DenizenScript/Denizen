package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityKnockbackScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity knockback entity
    // entity knockback <entity>
    // <entity> knockback entity
    // <entity> knockback <entity>
    //
    // @Regex ^on [^\s]+ knockback [^\s]+$
    //
    // @Switch in <area>
    // @Switch with <item>
    //
    // @Cancellable true
    //
    // @Plugin Paper
    //
    // @Triggers when an entity is knockbacked from being hit.
    //
    // @Context
    // <context.entity> returns the EntityTag that was knocked back.
    // <context.attacker> returns the EntityTag of the attacking entity
    // <context.strength> returns an ElementTag representing the strength of the hit.
    // <context.velocity> returns an LocationTag as a vector of the knockback being applied.
    // <context.item> returns an ItemTag that was used to inflict the knockback.
    //
    //
    // @Player when the attacker or attacked entity is a player. Cannot be both.
    //
    // @NPC when the attacker or attacker entity is an NPC. Cannot be both.
    //
    // -->

    public EntityKnockbackScriptEvent() {
        instance = this;
    }

    public static EntityKnockbackScriptEvent instance;

    public EntityTag entity;
    public EntityTag damager;
    public ItemTag held;
    public LocationTag velocity;
    public ElementTag strength;
    public EntityKnockbackByEntityEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("knockback");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String attacker = path.eventArgLowerAt(0);
        String target =  path.eventArgLowerAt(2);

        if (!attacker.isEmpty()) {
            if (damager != null) {
                if (!tryEntity(damager, attacker)) {
                    return false;
                }
            }
        }

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        if (!runWithCheck(path, held)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityKnockback";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(damager != null && damager.isPlayer() ? damager.getDenizenPlayer() : entity.isPlayer() ? entity.getDenizenPlayer() : null,
                damager != null && damager.isCitizensNPC() ? damager.getDenizenNPC() : entity.isCitizensNPC() ? EntityTag.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("strength")) {
            return strength;
        }
        else if (name.equals("velocity")) {
            return velocity;
        }
        else if (name.equals("item")) {
            return held;
        }
        else if (name.equals("damager") && damager != null) {
            return damager.getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityKnockback(EntityKnockbackByEntityEvent event) {
        entity = new EntityTag(event.getEntity());
        damager = null;
        strength = new ElementTag(event.getKnockbackStrength());
        velocity = new LocationTag(event.getAcceleration().toLocation(entity.getWorld()));
        damager = new EntityTag(((EntityKnockbackByEntityEvent) event).getHitBy());
        if (damager != null) {
            held = damager.getItemInHand();
        }
        this.event = event;
        fire(event);
    }
}
