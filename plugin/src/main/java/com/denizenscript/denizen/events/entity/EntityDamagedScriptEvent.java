package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamagedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[language]
    // @name Damage Cause
    // @group Useful Lists
    // @description
    // Possible damage causes: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html>
    // These are used in <@link event entity damage>, <@link tag server.damage_causes>, <@link tag EntityTag.last_damage.cause>, ...
    // -->

    // <--[event]
    // @Events
    // entity damaged (by <cause>)
    // <entity> damaged (by <cause>)
    // entity damages entity
    // entity damages <entity>
    // entity damaged by entity
    // entity damaged by <entity>
    // <entity> damages entity
    // <entity> damaged by entity
    // <entity> damaged by <entity>
    // <entity> damages <entity>
    //
    // @Regex ^on [^\s]+ ((damages [^\s]+)|damaged( by [^\s]+)?)$
    //
    // @Group Entity
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Switch with:<item> to only process the event when the item used to cause damage (in the damager's hand) is a specified item.
    //
    // @Cancellable true
    //
    // @Triggers when an entity is damaged.
    //
    // @Context
    // <context.entity> returns the EntityTag that was damaged.
    // <context.damager> returns the EntityTag damaging the other entity, if any.
    // <context.cause> returns the an ElementTag of reason the entity was damaged - see <@link language damage cause> for causes.
    // <context.damage> returns an ElementTag(Decimal) of the amount of damage dealt.
    // <context.final_damage> returns an ElementTag(Decimal) of the amount of damage dealt, after armor is calculated.
    // <context.projectile> returns a EntityTag of the projectile, if one caused the event.
    // <context.damage_type_map> returns a MapTag the damage dealt by a specific damage type with keys: BASE, HARD_HAT, BLOCKING, ARMOR, RESISTANCE, MAGIC, ABSORPTION.
    //
    // @Determine
    // ElementTag(Decimal) to set the amount of damage the entity receives.
    //
    // @Player when the damager or damaged entity is a player. Cannot be both.
    //
    // @NPC when the damager or damaged entity is an NPC. Cannot be both.
    //
    // -->

    public EntityDamagedScriptEvent() {
        instance = this;
    }

    public static EntityDamagedScriptEvent instance;

    public EntityTag entity;
    public ElementTag cause;
    public EntityTag damager;
    public EntityTag projectile;
    public ItemTag held;
    public EntityDamageEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (!cmd.equals("damaged") && !cmd.equals("damages")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        String attacker = cmd.equals("damages") ? path.eventArgLowerAt(0) :
                path.eventArgLowerAt(2).equals("by") ? path.eventArgLowerAt(3) : "";
        String target = cmd.equals("damages") ? path.eventArgLowerAt(2) : path.eventArgLowerAt(0);
        if (!attacker.isEmpty()) {
            if (damager != null) {
                if (!cause.asString().equals(attacker) && !tryEntity(projectile, attacker) && !tryEntity(damager, attacker)) {
                    return false;
                }
            }
            else {
                if (!cause.asString().equals(attacker)) {
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
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "EntityDamaged";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isDouble()) {
            event.setDamage(((ElementTag) determinationObj).asDouble());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(damager != null && damager.isPlayer() ? damager.getDenizenPlayer() : entity.isPlayer() ? entity.getDenizenPlayer() : null,
                damager != null && damager.isCitizensNPC() ? damager.getDenizenNPC() : entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("damage")) {
            return new ElementTag(event.getDamage());
        }
        else if (name.equals("final_damage")) {
            return new ElementTag(event.getFinalDamage());
        }
        else if (name.equals("cause")) {
            return cause;
        }
        else if (name.equals("damager") && damager != null) {
            return damager.getDenizenObject();
        }
        else if (name.equals("projectile") && projectile != null) {
            return projectile.getDenizenObject();
        }
        else if (name.equals("damage_type_map")) {
            MapTag map = new MapTag();
            for (EntityDamageEvent.DamageModifier dm : EntityDamageEvent.DamageModifier.values()) {
                map.putObject(dm.name(), new ElementTag(event.getDamage(dm)));
            }
            return map;
        }
        else if (name.startsWith("damage_")) {
            Deprecations.damageEventTypeMap.warn();
            for (EntityDamageEvent.DamageModifier dm : EntityDamageEvent.DamageModifier.values()) {
                if (name.equals("damage_" + CoreUtilities.toLowerCase(dm.name()))) {
                    return new ElementTag(event.getDamage(dm));
                }
            }
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        entity = new EntityTag(event.getEntity());
        cause = new ElementTag(CoreUtilities.toLowerCase(event.getCause().name()));
        damager = null;
        projectile = null;
        held = null;
        if (event instanceof EntityDamageByEntityEvent) {
            damager = new EntityTag(((EntityDamageByEntityEvent) event).getDamager());
            if (damager.isProjectile()) {
                projectile = damager;
                if (damager.hasShooter()) {
                    damager = damager.getShooter();
                }
            }
            if (damager != null) {
                held = damager.getItemInHand();
                if (held != null) {
                    held.setAmount(1);
                }
            }
        }
        this.event = event;
        fire(event);
    }
}
