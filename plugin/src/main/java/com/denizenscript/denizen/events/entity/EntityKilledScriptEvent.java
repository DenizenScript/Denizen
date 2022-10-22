package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityKilledScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> killed (by <'cause'>)
    // <entity> killed (by <entity>)
    // <entity> kills <entity>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity is killed.
    //
    // @Warning This event may mis-fire in some cases, particularly with plugins or scripts modify the damage from scripts. If you need reliable death tracking, the entity death event may be better.
    //
    // @Context
    // <context.entity> returns the EntityTag that was killed.
    // <context.cause> returns an ElementTag of reason the entity was damaged - see <@link language damage cause> for causes.
    // <context.damage> returns an ElementTag(Decimal) of the amount of damage dealt.
    // <context.final_damage> returns an ElementTag(Decimal) of the amount of damage dealt, after armor is calculated.
    // <context.damager> returns the EntityTag damaging the other entity.
    // <context.projectile> returns a EntityTag of the projectile shot by the damager, if any.
    // <context.damage_type_map> returns a MapTag the damage dealt by a specific damage type with keys: BASE, HARD_HAT, BLOCKING, ARMOR, RESISTANCE, MAGIC, ABSORPTION.
    //
    // @Determine
    // ElementTag(Decimal) to set the amount of damage the entity receives, instead of dying.
    //
    // @Player when the killer or entity that was killed is a player. Cannot be both.
    //
    // @NPC when the killer or entity that was killed is an NPC. Cannot be both.
    //
    // -->

    public EntityKilledScriptEvent() {
        registerCouldMatcher("<entity> killed (by <'cause'>)");
        registerCouldMatcher("<entity> killed (by <entity>)");
        registerCouldMatcher("<entity> kills <entity>");
    }


    public EntityTag entity;
    public ElementTag final_damage;
    public EntityTag damager;
    public EntityTag projectile;
    public EntityDamageEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        String arg0 = path.eventArgLowerAt(0);
        String arg2 = path.eventArgLowerAt(2);
        String arg3 = path.eventArgLowerAt(3);
        String attacker = cmd.equals("kills") ? arg0 : arg2.equals("by") ? arg3 : "";
        String target = cmd.equals("kills") ? arg2 : arg0;
        if (!attacker.isEmpty()) {
            if (damager != null) {
                if (!runGenericCheck(attacker, event.getCause().name()) && (projectile == null || !projectile.tryAdvancedMatcher(attacker)) && (damager == null || !damager.tryAdvancedMatcher(attacker))) {
                    return false;
                }
            }
            else if (!runGenericCheck(attacker, event.getCause().name())) {
                return false;
            }
        }
        if (!entity.tryAdvancedMatcher(target)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
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
        PlayerTag player = entity.isPlayer() ? EntityTag.getPlayerFrom(event.getEntity()) : null;
        if (damager != null && player == null && damager.isPlayer()) {
            player = EntityTag.getPlayerFrom(damager.getBukkitEntity());
        }
        NPCTag npc = entity.isCitizensNPC() ? entity.getDenizenNPC() : null;
        if (damager != null && npc == null && damager.isCitizensNPC()) {
            npc = damager.getDenizenNPC();
        }
        return new BukkitScriptEntryData(player, npc);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity": return entity.getDenizenEntity();
            case "damage": return new ElementTag(event.getDamage());
            case "final_damage": return final_damage;
            case "cause": return new ElementTag(event.getCause());
            case "damager": return damager != null ? damager.getDenizenObject() : null;
            case "projectile": return  projectile != null ? projectile.getDenizenObject() : null;
            case "damage_type_map":
                MapTag map = new MapTag();
                for (EntityDamageEvent.DamageModifier dm : EntityDamageEvent.DamageModifier.values()) {
                    map.putObject(dm.name(), new ElementTag(event.getDamage(dm)));
                }
                return map;
        }
        if (name.startsWith("damage_")) {
            BukkitImplDeprecations.damageEventTypeMap.warn();
            for (EntityDamageEvent.DamageModifier dm : EntityDamageEvent.DamageModifier.values()) {
                if (name.equals("damage_" + CoreUtilities.toLowerCase(dm.name()))) {
                    return new ElementTag(event.getDamage(dm));
                }
            }
        }
        return super.getContext(name);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityKilled(EntityDamageEvent event) {
        entity = new EntityTag(event.getEntity());
        // Check for possibility of death first
        if (entity.isValid() && entity.isLivingEntity()) {
            if (event.getFinalDamage() < entity.getLivingEntity().getHealth()) {
                return;
            }
        }
        else {
            return;
        }
        final_damage = new ElementTag(event.getFinalDamage());
        damager = null;
        projectile = null;
        if (event instanceof EntityDamageByEntityEvent) {
            damager = new EntityTag(((EntityDamageByEntityEvent) event).getDamager());
            if (damager.isProjectile()) {
                projectile = damager;
                if (damager.hasShooter()) {
                    damager = damager.getShooter();
                }
            }
        }
        this.event = event;
        fire(event);
    }
}
