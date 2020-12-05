package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.TagManager;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationStuckEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class NPCTagBase implements Listener {

    public NPCTagBase() {

        // <--[tag]
        // @attribute <npc[(<npc>)]>
        // @returns NPCTag
        // @description
        // Returns an npc object constructed from the input value.
        // Refer to <@link language NPCTag objects>.
        // If no input value is specified, returns the linked NPC.
        // -->
        if (Depends.citizens != null) {
            Bukkit.getServer().getPluginManager().registerEvents(this, Denizen.getInstance());
            TagManager.registerTagHandler("npc", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    NPCTag npc = ((BukkitTagContext) attribute.context).npc;
                    if (npc != null) {
                        return npc;
                    }
                    else {
                        attribute.echoError("Missing NPC for npc tag.");
                        return null;
                    }
                }
                return NPCTag.valueOf(attribute.getContext(1), attribute.context);
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
                        + entity.getType().toString(), player);
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
        String determination2 = npc.action("stuck", null, context);
        if (determination2.equalsIgnoreCase("none")) {
            event.setAction(null);
        }
        if (determination2.equalsIgnoreCase("teleport")) {
            event.setAction(TeleportStuckAction.INSTANCE);
        }

    }
}
