package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityKilledScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity killed (in <area>)
    // entity killed by <cause> (in <area>)
    // entity killed by entity (in <area>)
    // entity killed by <entity> (in <area>)
    // <entity> killed (in <area>)
    // <entity> killed by <cause> (in <area>)
    // <entity> killed by entity (in <area>)
    // <entity> killed by <entity> (in <area>)
    // entity kills entity (in <area>)
    // entity kills <entity> (in <area>)
    // <entity> kills entity (in <area>)
    // <entity> kills <entity> (in <area>)
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
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("killed") || cmd.equals("kills");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        String arg0 = CoreUtilities.getXthArg(0, lower);
        String arg2 = CoreUtilities.getXthArg(2, lower);
        String arg3 = CoreUtilities.getXthArg(3, lower);
        String attacker = cmd.equals("kills") ? arg0 : arg2.equals("by") ? arg3 : "";
        String target = cmd.equals("kills") ? arg2 : arg0;
        if (attacker.length() > 0) {
            if (damager != null) {
                if (!damager.matchesEntity(attacker) && !cause.asString().equals(attacker)) {
                    return false;
                }
            }
            else if (!cause.asString().equals(attacker)) {
                return false;
            }
        }
        if (!entity.matchesEntity(target)) {
            return false;
        }

        return runInCheck(scriptContainer, s, lower, entity.getLocation());
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
        if (aH.matchesDouble(determination)) {
            damage = new Element(aH.getDoubleFrom(determination));
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        dPlayer player = entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null;
        if (damager != null && player == null && damager.isPlayer()) {
            player = dEntity.getPlayerFrom(damager.getBukkitEntity());
        }
        dNPC npc = entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null;
        if (damager != null && npc == null && damager.isCitizensNPC()) {
            npc = dEntity.getNPCFrom(damager.getBukkitEntity());
        }
        return new BukkitScriptEntryData(player, npc);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("damage")) {
            return damage;
        }
        else if (name.equals("final_damage")) {
            return final_damage;
        }
        else if (name.equals("cause")) {
            return cause;
        }
        else if ((name.equals("damager")) && (damager != null)) {
            return damager;
        }
        else if ((name.equals("projectile")) && (projectile != null)) {
            return projectile;
        }
        else if (name.startsWith("damage_")) {
            for (EntityDamageEvent.DamageModifier dm : EntityDamageEvent.DamageModifier.values()) {
                if (name.equals("damage_" + CoreUtilities.toLowerCase(dm.name()))) {
                    return new Element(event.getDamage(dm));
                }
            }
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityKilled(EntityDamageEvent event) {
        entity = new dEntity(event.getEntity());
        // Check for possibility of death first
        if (entity.isValid() && entity.isLivingEntity()) {
            if (event.getFinalDamage() < entity.getLivingEntity().getHealth()) {
                return;
            }
        }
        else {
            return;
        }
        damage = new Element(event.getDamage());
        final_damage = new Element(event.getFinalDamage());
        cause = new Element(event.getCause().name().toLowerCase());
        damager = null;
        projectile = null;
        if (event instanceof EntityDamageByEntityEvent) {
            damager = new dEntity(((EntityDamageByEntityEvent) event).getDamager());
            if (damager.isProjectile()) {
                projectile = damager;
                if (damager.hasShooter()) {
                    damager = damager.getShooter();
                }
            }
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setDamage(damage.asDouble());
    }
}
