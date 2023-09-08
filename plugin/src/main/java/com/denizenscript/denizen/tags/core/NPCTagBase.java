package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationStuckEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;

public class NPCTagBase implements Listener {

    public NPCTagBase() {

        // <--[tag]
        // @attribute <npc[(<npc>)]>
        // @returns NPCTag
        // @description
        // Returns an npc object constructed from the input value.
        // Refer to <@link objecttype NPCTag>.
        // If no input value is specified, returns the linked NPC.
        // -->
        if (Depends.citizens != null) {
            Bukkit.getServer().getPluginManager().registerEvents(this, Denizen.getInstance());
            TagManager.registerTagHandler(NPCTag.class, "npc", (attribute) -> {
                if (!attribute.hasParam()) {
                    NPCTag npc = ((BukkitTagContext) attribute.context).npc;
                    if (npc != null) {
                        return npc;
                    }
                    else {
                        attribute.echoError("Missing NPC for npc tag.");
                        return null;
                    }
                }
                return NPCTag.valueOf(attribute.getParam(), attribute.context);
            });
        }
    }

    ///////
    // Keep track of previous locations and fire navigation actions
    ////

    public static Map<Integer, LocationTag> previousLocations = new HashMap<>();

    // <--[action]
    // @Actions
    // complete navigation
    //
    // @Triggers when the NPC has finished a 'walk' command,
    // or has reached a path point.
    //
    // @Context
    // None
    //
    // -->
    @EventHandler
    public void navComplete(NavigationCompleteEvent event) {
        // Do the assignment script action
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) {
            return;
        }
        NPCTag npc = new NPCTag(event.getNPC());

        npc.action("complete navigation", null);

    }

    // <--[action]
    // @Actions
    // begin navigation
    //
    // @Triggers when the NPC has received a 'walk' command,
    // or is about to follow a path.
    //
    // @Context
    // None
    //
    // -->
    @EventHandler
    public void navBegin(NavigationBeginEvent event) {
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) {
            return;
        }
        NPCTag npc = new NPCTag(event.getNPC());
        npc.action("begin navigation", null);
        if (event.getNPC().getNavigator().getTargetType() == TargetType.ENTITY) {
            LivingEntity entity = (LivingEntity) event.getNPC().getNavigator().getEntityTarget().getTarget();

            // If the NPC has an entity target, is aggressive towards it
            // and that entity is not dead, trigger "on attack" command
            if (event.getNPC().getNavigator().getEntityTarget().isAggressive()
                    && !entity.isDead()) {

                PlayerTag player = null;

                // Check if the entity attacked by this NPC is a player
                if (entity instanceof Player) {
                    player = PlayerTag.mirrorBukkitPlayer((Player) entity);
                }

                // <--[action]
                // @Actions
                // attack
                // attack on <entity>
                //
                // @Triggers when the NPC is about to attack an enemy.
                //
                // @Context
                // None
                //
                // -->
                npc.action("attack", player);

                npc.action("attack on "
                        + entity.getType(), player);
            }
            previousLocations.put(event.getNPC().getId(), npc.getLocation());
        }
    }

    // <--[action]
    // @Actions
    // cancel navigation
    // cancel navigation due to <reason>
    //
    // @Triggers when a plugin or script cancels an NPC's navigation.
    //
    // @Context
    // None
    //
    // -->
    @EventHandler
    public void navCancel(NavigationCancelEvent event) {
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) {
            return;
        }
        NPCTag npc = new NPCTag(event.getNPC());
        npc.action("cancel navigation", null);
        npc.action("cancel navigation due to " + event.getCancelReason().toString(), null);
    }

    // <--[action]
    // @Actions
    // stuck
    //
    // @Triggers when the NPC's navigator is stuck.
    //
    // @Context
    // <context.action> returns 'teleport' or 'none'
    //
    // @Determine
    // "NONE" to do nothing.
    // "TELEPORT" to teleport.
    //
    // -->
    @EventHandler
    public void navStuck(NavigationStuckEvent event) {
        // Do the assignment script action
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) {
            return;
        }
        NPCTag npc = new NPCTag(event.getNPC());
        Map<String, ObjectTag> context = new HashMap<>();
        context.put("action", new ElementTag(event.getAction() == TeleportStuckAction.INSTANCE ? "teleport" : "none"));
        ListTag determination = npc.action("stuck", null, context);
        if (determination.containsCaseInsensitive("none")) {
            event.setAction(null);
        }
        if (determination.containsCaseInsensitive("teleport")) {
            event.setAction(TeleportStuckAction.INSTANCE);
        }
    }

    // <--[action]
    // @Actions
    // death
    // death by entity
    // death by <entity>
    // death by block
    // death by <cause>
    //
    // @Triggers when the NPC dies.
    //
    // @Context
    // <context.killer> returns the entity that killed the NPC (if any)
    // <context.shooter> returns the shooter of the killing projectile (if any)
    // <context.damage> returns the last amount of damage applied (if any)
    // <context.death_cause> returns the last damage cause (if any)
    //
    // -->
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent deathEvent) {
        NPC citizen = CitizensAPI.getNPCRegistry().getNPC(deathEvent.getEntity());
        if (citizen == null || !citizen.hasTrait(AssignmentTrait.class)) {
            return;
        }
        NPCTag npc = new NPCTag(citizen);
        EntityDamageEvent event = deathEvent.getEntity().getLastDamageCause();
        String deathCause = event == null ? "unknown" : CoreUtilities.toLowerCase(event.getCause().toString()).replace('_', ' ');
        Map<String, ObjectTag> context = new HashMap<>();
        context.put("damage", new ElementTag(event == null ? 0 : event.getDamage()));
        context.put("death_cause", new ElementTag(deathCause));
        PlayerTag player = null;
        if (event instanceof EntityDamageByEntityEvent) {
            Entity killerEntity = ((EntityDamageByEntityEvent) event).getDamager();
            context.put("killer", new EntityTag(killerEntity).getDenizenObject());
            if (killerEntity instanceof Player) {
                player = PlayerTag.mirrorBukkitPlayer((Player) killerEntity);
            }
            else if (killerEntity instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) killerEntity).getShooter();
                if (shooter instanceof LivingEntity) {
                    context.put("shooter", new EntityTag((LivingEntity) shooter).getDenizenObject());
                    if (shooter instanceof Player) {
                        player = PlayerTag.mirrorBukkitPlayer((Player) shooter);
                    }
                    npc.action("death by " + ((LivingEntity) shooter).getType(), player, context);
                }
            }
            npc.action("death by entity", player, context);
            npc.action("death by " + killerEntity.getType(), player, context);
        }
        else if (event instanceof EntityDamageByBlockEvent) {
            npc.action("death by block", null, context);
        }
        npc.action("death", player, context);
        npc.action("death by " + deathCause, player, context);
    }

    // <--[action]
    // @Actions
    // hit
    // hit on <entity>
    //
    // @Triggers when the NPC hits an enemy.
    //
    // @Context
    // None
    //
    // -->
    // <--[action]
    // @Actions
    // kill
    // kill of <entity>
    //
    // @Triggers when the NPC kills an enemy.
    //
    // @Context
    // None
    //
    // -->
    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        NPC citizen = CitizensAPI.getNPCRegistry().getNPC(event.getDamager());
        if (citizen == null) {
            if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Entity) {
                    citizen = CitizensAPI.getNPCRegistry().getNPC((Entity) ((Projectile) event.getDamager()).getShooter());
                }
            }
        }
        if (citizen == null || !citizen.hasTrait(AssignmentTrait.class)) {
            return;
        }
        NPCTag npc = new NPCTag(citizen);
        PlayerTag player = null;
        if (event.getEntity() instanceof Player) {
            player = PlayerTag.mirrorBukkitPlayer((Player) event.getEntity());
        }
        // TODO: Context containing the entity hit
        npc.action("hit", player);
        npc.action("hit on " + event.getEntityType().name(), player);
        if (event.getEntity() instanceof LivingEntity) {
            if (((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() <= 0) {
                npc.action("kill", player);
                npc.action("kill of " + event.getEntityType().name(), player);
            }
        }
    }
}
