package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityDamageSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on (e@)?\\w+ (damaged by|damages|killed by|kills|damaged|killed)( \\w+)?", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Event names are simple enough to just go ahead and pass on any match.
                return true;
            }
        }
        // No matches at all, just fail.
        return false;
    }


    @Override
    public void _initialize() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Entity Damage SmartEvent.");
    }


    @Override
    public void breakDown() {
        EntityDamageEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[language]
    // @name Damage Cause
    // @group Events
    // @description
    // Possible damage causes
    // BLOCK_EXPLOSION, CONTACT, CUSTOM, DROWNING, ENTITY_ATTACK, ENTITY_EXPLOSION,
    // FALL, FALLING_BLOCK, FIRE, FIRE_TICK, LAVA, LIGHTNING, MAGIC, MELTING, POISON,
    // PROJECTILE, STARVATION, SUFFOCATION, SUICIDE, THORNS, VOID, WITHER.
    // -->

    // <--[event]
    // @Events
    // entity damaged
    // entity damaged by <cause>
    // <entity> damaged
    // <entity> damaged by <cause>
    //
    // @Triggers when an entity is damaged.
    // @Context
    // <context.cause> returns the an Element of reason the entity was damaged - see <@link language damage cause> for causes.
    // <context.damage> returns an Element(Decimal) of the amount of damage dealt.
    // <context.final_damage> returns an Element(Decimal) of the amount of damage dealt, after armor is calculated.
    // <context.entity> returns the dEntity that was damaged.
    // <context.damage_TYPE> returns the damage dealt by a specific damage type where TYPE can be any of: BASE, HARD_HAT, BLOCKING, ARMOR, RESISTANCE, MAGIC, ABSORPTION.
    //
    // @Determine
    // "CANCELLED" to stop the entity from being damaged.
    // Element(Decimal) to set the amount of damage the entity receives.
    //
    // -->
    @EventHandler
    public void entityDamage(EntityDamageEvent event) {

        dPlayer player = null;
        dNPC npc = null;
        String determination;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        String cause = event.getCause().name();

        context.put("entity", entity.getDenizenObject());
        context.put("damage", new Element(event.getDamage()));
        context.put("final_damage", new Element(event.getFinalDamage()));
        context.put("cause", new Element(event.getCause().name()));
        for (EntityDamageEvent.DamageModifier dm: EntityDamageEvent.DamageModifier.values()) {
            context.put("damage_" +  dm.name(), new Element(event.getDamage(dm)));
        }

        if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getDenizenPlayer();

        boolean isFatal = false;

        if (entity.isValid() && entity.isLivingEntity()) {
            if (event.getFinalDamage() >= entity.getLivingEntity().getHealth()) {
                isFatal = true;
            }
        }

        List<String> events = new ArrayList<String>();
        events.add("entity damaged");
        events.add("entity damaged by " + cause);
        events.add(entity.identifyType() + " damaged");
        events.add(entity.identifyType() + " damaged by " + cause);
        events.add(entity.identifySimple() + " damaged");
        events.add(entity.identifySimple() + " damaged by " + cause);

        if (isFatal) {

            // <--[event]
            // @Events
            // entity killed
            // entity killed by <cause>
            // <entity> killed
            // <entity> killed by <cause>
            //
            // @Triggers when an entity is killed.
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
            // "CANCELLED" to stop the entity from being killed.
            // Element(Decimal) to set the amount of damage the entity receives, instead of dying.
            //
            // -->

            events.add("entity killed");
            events.add("entity killed by " + cause);
            events.add(entity.identifyType() + " killed");
            events.add(entity.identifyType() + " killed by " + cause);
            events.add(entity.identifySimple() + " killed");
            events.add(entity.identifySimple() + " killed by " + cause);
        }

        if (event instanceof EntityDamageByEntityEvent) {

            // <--[event]
            // @Events
            // entity damages entity
            // entity damages <entity>
            // entity damaged by entity
            // entity damaged by <entity>
            // <entity> damages entity
            // <entity> damaged by entity
            // <entity> damaged by <entity>
            // <entity> damages <entity>
            //
            // @Triggers when an entity damages another entity.
            // @Context
            // <context.entity> returns the dEntity that was damaged.
            // <context.cause> returns the an Element of reason the entity was damaged - see <@link language damage cause> for causes.
            // <context.damage> returns an Element(Decimal) of the amount of damage dealt.
            // <context.final_damage> returns an Element(Decimal) of the amount of damage dealt, after armor is calculated.
            // <context.damager> returns the dEntity damaging the other entity.
            // <context.projectile> returns a dEntity of the projectile, if one caused the event.
            // <context.damage_TYPE> returns the damage dealt by a specific damage type where TYPE can be any of: BASE, HARD_HAT, BLOCKING, ARMOR, RESISTANCE, MAGIC, ABSORPTION.
            //
            // @Determine
            // "CANCELLED" to stop the entity from being damaged.
            // Element(Decimal) to set the amount of damage the entity receives.
            //
            // -->

            EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;

            // Have a different set of player and NPC contexts for events
            // like "player damages player" from the one we have for
            // "player damaged by player"

            dPlayer subPlayer = null;
            dNPC subNPC = null;
            dEntity projectile = null;
            dEntity damager = new dEntity(subEvent.getDamager());

            if (damager.isProjectile()) {
                projectile = damager;
                context.put("projectile", projectile);

                if (damager.hasShooter()) {
                    damager = damager.getShooter();
                }

                if (!damager.getEntityType().equals(projectile.getEntityType())) {
                    events.add("entity damaged by projectile");
                    events.add("entity damaged by " + projectile.identifyType());
                    events.add(entity.identifyType() + " damaged by " + projectile.identifyType());
                    events.add(entity.identifySimple() + " damaged by " + projectile.identifyType());
                    events.add(entity.identifySimple() + " damaged by projectile");
                    events.add("entity damaged by " + projectile.identifyType());
                    events.add(entity.identifyType() + " damaged by " + projectile.identifyType());
                    events.add(entity.identifySimple() + " damaged by " + projectile.identifyType());
                }
            }

            context.put("damager", damager.getDenizenObject());

            events.add("entity damaged by entity");
            events.add("entity damaged by " + damager.identifyType());
            events.add("entity damaged by " + damager.identifySimple());
            events.add(entity.identifyType() + " damaged by entity");
            events.add(entity.identifyType() + " damaged by " + damager.identifyType());
            events.add(entity.identifyType() + " damaged by " + damager.identifySimple());
            events.add(entity.identifySimple() + " damaged by entity");
            events.add(entity.identifySimple() + " damaged by " + damager.identifyType());
            events.add(entity.identifySimple() + " damaged by " + damager.identifySimple());

            if (damager.isCitizensNPC()) {
                subNPC = damager.getDenizenNPC();

                // If we had no NPC in our regular context, use this one
                if (npc == null) npc = subNPC;
            }

            else if (damager.isPlayer()) {
                subPlayer = damager.getDenizenPlayer();

                // If we had no player in our regular context, use this one
                if (player == null) player = subPlayer;
            }

            // Have a new list of events for the subContextPlayer
            // and subContextNPC

            List<String> subEvents = new ArrayList<String>();

            subEvents.add("entity damages entity");
            subEvents.add("entity damages " + entity.identifyType());
            subEvents.add("entity damages " + entity.identifySimple());
            subEvents.add(damager.identifyType() + " damages entity");
            subEvents.add(damager.identifyType() + " damages " + entity.identifyType());
            subEvents.add(damager.identifyType() + " damages " + entity.identifySimple());
            subEvents.add(damager.identifySimple() + " damages entity");
            subEvents.add(damager.identifySimple() + " damages " + entity.identifyType());
            subEvents.add(damager.identifySimple() + " damages " + entity.identifySimple());

            if (projectile != null && !damager.getEntityType().equals(projectile.getEntityType())) {
                subEvents.add("projectile damages entity");
                subEvents.add("projectile damages " + entity.identifyType());
                subEvents.add("projectile damages " + entity.identifySimple());
                subEvents.add(projectile.identifyType() + " damages entity");
                subEvents.add(projectile.identifyType() + " damages " + entity.identifyType());
                subEvents.add(projectile.identifyType() + " damages " + entity.identifySimple());
                subEvents.add(projectile.identifySimple() + " damages entity");
                subEvents.add(projectile.identifySimple() + " damages " + entity.identifyType());
                subEvents.add(projectile.identifySimple() + " damages " + entity.identifySimple());
            }

            if (isFatal) {

                events.add("entity killed by entity");
                events.add("entity killed by " + damager.identifyType());
                events.add("entity killed by " + damager.identifySimple());
                events.add(entity.identifyType() + " killed by entity");
                events.add(entity.identifyType() + " killed by " + damager.identifyType());
                events.add(entity.identifyType() + " killed by " + damager.identifySimple());
                events.add(entity.identifySimple() + " killed by entity");
                events.add(entity.identifySimple() + " killed by " + damager.identifyType());
                events.add(entity.identifySimple() + " killed by " + damager.identifySimple());

                if (projectile != null && !damager.getEntityType().equals(projectile.getEntityType())) {
                    events.add("entity killed by projectile");
                    events.add("entity killed by " + projectile.identifyType());
                    events.add(entity.identifyType() + " killed by projectile");
                    events.add(entity.identifyType() + " killed by " + projectile.identifyType());
                    events.add(entity.identifyType() + " killed by " + projectile.identifySimple());
                    events.add(entity.identifySimple() + " killed by projectile");
                    events.add(entity.identifySimple() + " killed by " + projectile.identifyType());
                    events.add(entity.identifySimple() + " killed by " + projectile.identifySimple());
                }

                // <--[event]
                // @Events
                // entity kills entity
                // entity kills <entity>
                // <entity> kills entity
                // <entity> kills <entity>
                //
                // @Triggers when an entity kills another entity.
                // @Context
                // <context.entity> returns the dEntity that was killed.
                // <context.cause> returns the an Element of reason the entity was damaged - see <@link language damage cause> for causes.
                // <context.damage> returns an Element(Decimal) of the amount of damage dealt.
                // <context.final_damage> returns an Element(Decimal) of the amount of damage dealt, after armor is calculated.
                // <context.projectile> returns a dEntity of the projectile, if one caused the event.
                // <context.damage_TYPE> returns the damage dealt by a specific damage type where TYPE can be any of: BASE, HARD_HAT, BLOCKING, ARMOR, RESISTANCE, MAGIC, ABSORPTION.
                //
                // @Determine
                // "CANCELLED" to stop the entity from being killed.
                // Element(Number) to set the amount of damage the entity receives, instead of dying.
                //
                // -->

                subEvents.add("entity kills entity");
                subEvents.add("entity kills " + entity.identifyType());
                subEvents.add("entity kills " + entity.identifySimple());
                subEvents.add(damager.identifyType() + " kills entity");
                subEvents.add(damager.identifyType() + " kills " + entity.identifyType());
                subEvents.add(damager.identifyType() + " kills " + entity.identifySimple());
                subEvents.add(damager.identifySimple() + " kills entity");
                subEvents.add(damager.identifySimple() + " kills " + entity.identifyType());
                subEvents.add(damager.identifySimple() + " kills " + entity.identifySimple());

                if (projectile != null && !damager.getEntityType().equals(projectile.getEntityType())) {
                    subEvents.add("projectile kills entity");
                    subEvents.add(projectile.identifyType() + " kills entity");
                    subEvents.add(projectile.identifyType() + " kills " + entity.identifyType());
                    subEvents.add(projectile.identifyType() + " kills " + entity.identifySimple());
                    subEvents.add(projectile.identifySimple() + " kills entity");
                    subEvents.add(projectile.identifySimple() + " kills " + entity.identifyType());
                    subEvents.add(projectile.identifySimple() + " kills " + entity.identifySimple());
                }
            }

            determination = BukkitWorldScriptHelper.doEvents(subEvents, subNPC, subPlayer, context, true);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);

            else if (aH.Argument.valueOf(determination)
                    .matchesPrimitive(aH.PrimitiveType.Double)) {
                event.setDamage(aH.getDoubleFrom(determination));
            }
        }

        determination = BukkitWorldScriptHelper.doEvents(events, npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (aH.Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Double)) {
            event.setDamage(aH.getDoubleFrom(determination));
        }
    }
}
