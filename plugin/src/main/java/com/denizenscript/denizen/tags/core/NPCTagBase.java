package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.events.core.NPCNavigationSmartEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.events.OldEventManager;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCTagBase implements Listener {

    public NPCTagBase() {
        if (Depends.citizens != null) {
            Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
            TagManager.registerTagHandler(new TagRunnable.RootForm() {
                @Override
                public void run(ReplaceableTagEvent event) {
                    npcTags(event);
                }
            }, "npc");
        }
    }

    public void npcTags(ReplaceableTagEvent event) {

        if (!event.matches("npc") || event.replaced()) {
            return;
        }

        // Build a new attribute out of the raw_tag supplied in the script to be fulfilled
        Attribute attribute = event.getAttributes();

        // NPCTags require a... NPCTag!
        NPCTag n = ((BukkitTagContext) event.getContext()).npc;

        // Player tag may specify a new player in the <player[context]...> portion of the tag.
        if (attribute.hasContext(1))
        // Check if this is a valid player and update the PlayerTag object reference.
        {
            if (NPCTag.matches(attribute.getContext(1))) {
                n = NPCTag.valueOf(attribute.getContext(1), attribute.context);
            }
            else {
                if (!event.hasAlternative()) {
                    Debug.echoError("Could not match '" + attribute.getContext(1) + "' to a valid NPC!");
                }
                return;
            }
        }


        if (n == null || !n.isValid()) {
            if (!event.hasAlternative()) {
                Debug.echoError("Invalid or missing NPC for tag <" + event.raw_tag + ">!");
            }
            return;
        }

        event.setReplacedObject(CoreUtilities.autoAttrib(n, attribute.fulfill(1)));

    }


    ///////
    // Keep track of previous locations and fire navigation actions
    ////

    public static Map<Integer, LocationTag> previousLocations = new HashMap<>();

    // <--[event]
    // @Events
    // npc completes navigation
    //
    // @Regex ^on npc completes navigation$
    //
    // @Warning This event may fire very rapidly.
    //
    // @Triggers when an NPC finishes navigating.
    //
    // @Context
    // None
    //
    // -->

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

        NPCTag npc = DenizenAPI.getDenizenNPC(event.getNPC());

        // Do world script event 'On NPC Completes Navigation'
        if (NPCNavigationSmartEvent.IsActive()) {
            OldEventManager.doEvents(Arrays.asList
                    ("npc completes navigation"), new BukkitScriptEntryData(null, npc), null);
        }

        // Do the assignment script action
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) {
            return;
        }
        npc.action("complete navigation", null);

    }

    // <--[event]
    // @Events
    // npc begins navigation
    //
    // @Regex ^on npc begins navigation$
    //
    // @Warning This event may fire very rapidly.
    //
    // @Triggers when an NPC begins navigating.
    //
    // @Context
    // None
    //
    // -->

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
        NPCTag npc = DenizenAPI.getDenizenNPC(event.getNPC());

        // Do world script event 'On NPC Begins Navigation'
        if (NPCNavigationSmartEvent.IsActive()) {
            OldEventManager.doEvents(Arrays.asList
                    ("npc begins navigation"), new BukkitScriptEntryData(null, npc), null);
        }

        if (!event.getNPC().hasTrait(AssignmentTrait.class)) {
            return;
        }
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
        NPCTag npc = DenizenAPI.getDenizenNPC(event.getNPC());

        if (NPCNavigationSmartEvent.IsActive()) {
            OldEventManager.doEvents(Arrays.asList
                    ("npc cancels navigation"), new BukkitScriptEntryData(null, npc), null);
        }

        if (!event.getNPC().hasTrait(AssignmentTrait.class)) {
            return;
        }
        npc.action("cancel navigation", null);
        npc.action("cancel navigation due to " + event.getCancelReason().toString(), null);
    }

    // <--[event]
    // @Events
    // npc stuck
    //
    // @Regex ^on npc stuck$
    //
    // @Triggers when an NPC's navigator is stuck.
    //
    // @Context
    // <context.action> returns 'teleport' or 'none'
    //
    // @Determine
    // "NONE" to do nothing.
    // "TELEPORT" to teleport.
    // -->

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

        NPCTag npc = DenizenAPI.getDenizenNPC(event.getNPC());

        Map<String, ObjectTag> context = new HashMap<>();

        context.put("action", new ElementTag(event.getAction() == TeleportStuckAction.INSTANCE ? "teleport" : "none"));

        // Do world script event 'On NPC stuck'
        if (NPCNavigationSmartEvent.IsActive()) {
            List<String> determinations = OldEventManager.doEvents(Arrays.asList
                    ("npc stuck"), new BukkitScriptEntryData(null, npc), context);
            for (String determination : determinations) {
                if (determination.equalsIgnoreCase("none")) {
                    event.setAction(null);
                }
                if (determination.equalsIgnoreCase("teleport")) {
                    event.setAction(TeleportStuckAction.INSTANCE);
                }
            }
        }

        // Do the assignment script action
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) {
            return;
        }
        String determination2 = npc.action("stuck", null, context);
        if (determination2.equalsIgnoreCase("none")) {
            event.setAction(null);
        }
        if (determination2.equalsIgnoreCase("teleport")) {
            event.setAction(TeleportStuckAction.INSTANCE);
        }

    }
}
