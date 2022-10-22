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
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

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
    // <entity> damaged by <entity>
    // <entity> damaged (by <'cause'>)
    // <entity> damages <entity>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Switch with:<item> to only process the event when the item used to cause damage (in the damager's hand) is a specified item.
    // @Switch type:<entity> to only run if the entity damaged matches the entity input.
    //
    // @Cancellable true
    //
    // @Triggers when an entity is damaged.
    //
    // @Context
    // <context.entity> returns the EntityTag that was damaged.
    // <context.damager> returns the EntityTag damaging the other entity, if any.
    // <context.cause> returns an ElementTag of reason the entity was damaged - see <@link language damage cause> for causes.
    // <context.damage> returns an ElementTag(Decimal) of the amount of damage dealt.
    // <context.final_damage> returns an ElementTag(Decimal) of the amount of damage dealt, after armor is calculated.
    // <context.projectile> returns a EntityTag of the projectile, if one caused the event.
    // <context.damage_type_map> returns a MapTag the damage dealt by a specific damage type with keys: BASE, HARD_HAT, BLOCKING, ARMOR, RESISTANCE, MAGIC, ABSORPTION.
    // <context.was_critical> returns 'true' if the damage was a critical hit. (Warning: this value is calculated and not guaranteed to be correct if the event is altered).
    //
    // @Determine
    // ElementTag(Decimal) to set the amount of damage the entity receives.
    // "CLEAR_MODIFIERS" to zero out all damage modifiers other than "BASE", effectively making damage == final_damage.
    //
    // @Player when the damager or damaged entity is a player. Cannot be both.
    //
    // @NPC when the damager or damaged entity is an NPC. Cannot be both.
    //
    // @Example
    // on entity damaged:
    // - announce "A <context.entity.entity_type> took damage!"
    //
    // @Example
    // on player damages cow:
    // - announce "<player.name> damaged a cow at <context.entity.location.simple>"
    //
    // @Example
    // on player damages cow|sheep|chicken with:*_hoe:
    // - narrate "Whoa there farmer, you almost hurt your farm animals with that farmin' tool!"
    // - determine cancelled
    //
    // @Example
    // # This example disambiguates this event from the "vehicle damaged" event for specific vehicle entity types.
    // on entity damaged type:minecart:
    // - announce "A minecart took non-vehicular damage!"
    //
    // -->

    public EntityDamagedScriptEvent() {
        registerCouldMatcher("<entity> damaged (by <'cause'>)");
        registerCouldMatcher("<entity> damaged by <entity>");
        registerCouldMatcher("<entity> damages <entity>");
        registerSwitches("with", "type");
    }


    public EntityTag entity;
    public EntityTag damager;
    public EntityTag projectile;
    public ItemTag held;
    public EntityDamageEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        String cmd = path.eventArgLowerAt(1);
        if (cmd.equals("damaged")) {
            if (path.eventArgLowerAt(0).equals("vehicle")) {
                return false;
            }
        }
        else if (cmd.equals("damages")) {
            if (path.eventArgLowerAt(2).equals("vehicle")) {
                return false;
            }
        }
        else {
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
                if (!runGenericCheck(attacker, event.getCause().name()) && (projectile == null || !projectile.tryAdvancedMatcher(attacker)) && (damager == null || !damager.tryAdvancedMatcher(attacker))) {
                    return false;
                }
            }
            else {
                if (!runGenericCheck(attacker, event.getCause().name())) {
                    return false;
                }
            }
        }
        if (!entity.tryAdvancedMatcher(target) || !path.tryObjectSwitch("type", entity)) {
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
        if (determinationObj instanceof ElementTag) {
            if (CoreUtilities.equalsIgnoreCase(determinationObj.toString(), "clear_modifiers")) {
                for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                    if (modifier != EntityDamageEvent.DamageModifier.BASE && event.isApplicable(modifier)) {
                        event.setDamage(modifier, 0);
                    }
                }
                return true;
            }
            else if (((ElementTag) determinationObj).isDouble()) {
                event.setDamage(((ElementTag) determinationObj).asDouble());
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(damager != null && damager.isPlayer() ? damager.getDenizenPlayer() : entity.isPlayer() ? entity.getDenizenPlayer() : null,
                damager != null && damager.isCitizensNPC() ? damager.getDenizenNPC() : entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    public boolean calculateWasCritical() {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return false;
        }
        if (!damager.isPlayer()) {
            return false;
        }
        // This based on the source of NMS EntityHuman#attack(Entity entity)
        // boolean flag = f2 > 0.9F;
        // boolean flag2 = flag && this.fallDistance > 0.0F && !this.onGround && !this.isClimbing() && !this.isInWater() && !this.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && entity instanceof EntityLiving;
        if (event.getDamage() <= 0.9) {
            return false;
        }
        if (!(event.getEntity() instanceof LivingEntity)) {
            return false;
        }
        Player player = damager.getPlayer();
        if (player.getAttackCooldown() < 0.999) { // attack cooldown is also checked in that method earlier
            return false;
        }
        if (player.getFallDistance() <= 0 || player.isOnGround() || player.isClimbing() || player.isInWater()) {
            return false;
        }
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            return false;
        }
        if (player.isInsideVehicle()) {
            return false;
        }
        return true;
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity": return entity.getDenizenObject();
            case "damage": return new ElementTag(event.getDamage());
            case "final_damage": return new ElementTag(event.getFinalDamage());
            case "cause": return new ElementTag(event.getCause());
            case "damager":
                if (damager != null) {
                    return damager.getDenizenObject();
                }
                break;
            case "projectile":
                if (projectile != null) {
                    return projectile.getDenizenObject();
                }
                break;
            case "damage_type_map": {
                MapTag map = new MapTag();
                for (EntityDamageEvent.DamageModifier dm : EntityDamageEvent.DamageModifier.values()) {
                    map.putObject(dm.name(), new ElementTag(event.getDamage(dm)));
                }
                return map;
            }
            case "was_critical":
                return new ElementTag(calculateWasCritical());
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

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        entity = new EntityTag(event.getEntity());
        damager = null;
        projectile = null;
        held = null;
        if (event instanceof EntityDamageByEntityEvent) {
            damager = new EntityTag(((EntityDamageByEntityEvent) event).getDamager());
            EntityTag shooter = damager.getShooter();
            if (damager instanceof Projectile) {
                projectile = damager;
            }
            if (shooter != null) {
                projectile = damager;
                damager = shooter;
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
