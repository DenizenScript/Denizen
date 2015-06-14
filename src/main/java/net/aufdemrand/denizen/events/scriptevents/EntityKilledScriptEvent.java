package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;

public class EntityKilledScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity killed
    // entity killed by <cause>
    // <entity> killed
    // <entity> killed by <cause>
    // entity kills entity
    // entity kills <entity>
    // <entity> kills entity
    // <entity> kills <entity>
    //
    // @Cancellable true
    //
    // @Triggers when an entity is killed.
    //
    // @Context
    // <context.entity> returns the dEntity that was killed.
    // <context.cause> returns the an Element of reason the entity was damaged - see <@link language damage cause> for causes.
    // <context.damage> returns an Element(Decimal) of the amount of damage dealt.
    // <context.final_damage> returns an Element(Decimal) of the amount of damage dealt, after armor is calculated.
    // <context.damager> returns the dEntity damaging the other entity.
    // <context.projectile> returns a dEntity of the projectile shot by the damager, if any.
    // <context.damage_TYPE> returns the damage dealt by a specific damage type where TYPE can be any of: BASE, HARD_HAT, BLOCKING, ARMOR, RESISTANCE, MAGIC, ABSORPTION.
    //
    // @Determine
    // Element(Decimal) to set the amount of damage the entity receives, instead of dying.
    //
    // -->

    public EntityKilledScriptEvent() {
        instance = this;
    }

    public static EntityKilledScriptEvent instance;

    public dEntity entity;
    public Element cause;
    public Element damage;
    public Element final_damage;
    public dEntity damager;
    public dEntity projectile;
    public EntityDamageEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains(" kills")
                || lower.contains(" killed");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        boolean by = lower.contains(" by ");
        dEntity entOne = by ? entity: damager;
        if (!entOne.matchesEntity(CoreUtilities.getXthArg(0, s))) {
            return false;
        }

        dEntity entTwo = by ? damager: entity;
        if (!(!by && lower.contains("killed") || entTwo.matchesEntity(CoreUtilities.getXthArg(by ? 3: 2, s)))) {
            return false;
        }

        if (entity.isValid() && entity.isLivingEntity()) {
            if (final_damage.asDouble() <= entity.getLivingEntity().getHealth()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityKilled";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityDamageEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.Argument.valueOf(determination).matchesPrimitive(aH.PrimitiveType.Double)) {
            damage = new Element(aH.getDoubleFrom(determination));
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("damage", damage);
        context.put("final_damage", final_damage);
        context.put("cause", cause);
        context.put("damager", damager);
        if (projectile != null) {
            context.put("projectile", projectile);
        }
        for (EntityDamageEvent.DamageModifier dm: EntityDamageEvent.DamageModifier.values()) {
            context.put("damage_" +  dm.name(), new Element(event.getDamage(dm)));
        }

        return context;
    }

    @EventHandler
    public void onEntityKilled(EntityDamageEvent event) {
        entity = new dEntity(event.getEntity());
        damage = new Element(event.getDamage());
        final_damage = new Element(event.getFinalDamage());
        cause = new Element(event.getCause().name().toLowerCase());
        if (event instanceof EntityDamageByEntityEvent) {
            damager = new dEntity(((EntityDamageByEntityEvent) event).getDamager());
            if (damager.isProjectile()) {
                projectile = damager;
                if (damager.hasShooter()) {
                    damager = damager.getShooter();
                }
            }
        }
        if (damager == null) {
            return;
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setDamage(damage.asDouble());
    }
}
