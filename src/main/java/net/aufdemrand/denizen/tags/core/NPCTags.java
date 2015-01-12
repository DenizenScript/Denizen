package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.tags.ReplaceableTagEvent;
import net.aufdemrand.denizen.events.core.NPCNavigationSmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;

import net.citizensnpcs.api.ai.event.NavigationStuckEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NPCTags implements Listener {

    public NPCTags(Denizen denizen) {
        if (Depends.citizens != null) {
            denizen.getServer().getPluginManager().registerEvents(this, denizen);
            TagManager.registerTagEvents(this);
        }
    }

    @TagManager.TagEvents
    public void npcTags(ReplaceableTagEvent event) {

        if (!event.matches("npc") || event.replaced()) return;

        // Build a new attribute out of the raw_tag supplied in the script to be fulfilled
        Attribute attribute = event.getAttributes();

        // NPCTags require a... dNPC!
        dNPC n = ((BukkitTagContext)event.getContext()).npc;

        // Player tag may specify a new player in the <player[context]...> portion of the tag.
        if (attribute.hasContext(1))
            // Check if this is a valid player and update the dPlayer object reference.
            if (dNPC.matches(attribute.getContext(1)))
                n = dNPC.valueOf(attribute.getContext(1));
            else {
                if (!event.hasAlternative()) dB.echoError("Could not match '" + attribute.getContext(1) + "' to a valid NPC!");
                return;
            }


        if (n == null || !n.isValid()) {
            if (!event.hasAlternative()) dB.echoError("Invalid or missing NPC for tag <" + event.raw_tag + ">!");
            return;
        }

        event.setReplaced(n.getAttribute(attribute.fulfill(1)));

    }


    ///////
    // Keep track of previous locations and fire navigation actions
    ////

    public static Map<Integer, dLocation> previousLocations = new HashMap<Integer, dLocation>();

    // <--[event]
    // @Events
    // npc completes navigation
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

        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());

        // Do world script event 'On NPC Completes Navigation'
        if (NPCNavigationSmartEvent.IsActive())
            EventManager.doEvents(Arrays.asList
                    ("npc completes navigation"), npc, null, null);

        // Do the assignment script action
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        npc.action("complete navigation", null);

    }

    // <--[event]
    // @Events
    // npc begins navigation
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
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());

        // Do world script event 'On NPC Completes Navigation'
        if (NPCNavigationSmartEvent.IsActive())
            EventManager.doEvents(Arrays.asList
                    ("npc begins navigation"), npc, null, null);

        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        npc.action("begin navigation", null);

        if (event.getNPC().getNavigator().getTargetType() == TargetType.ENTITY) {
            LivingEntity entity = event.getNPC().getNavigator().getEntityTarget().getTarget();

            // If the NPC has an entity target, is aggressive towards it
            // and that entity is not dead, trigger "on attack" command
            if (event.getNPC().getNavigator().getEntityTarget().isAggressive()
                && !entity.isDead()) {

                dPlayer player = null;

                // Check if the entity attacked by this NPC is a player
                if (entity instanceof Player)
                    player = dPlayer.mirrorBukkitPlayer((Player) entity);

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
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());

        if (NPCNavigationSmartEvent.IsActive())
            EventManager.doEvents(Arrays.asList
                    ("npc cancels navigation"), npc, null, null);

        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        npc.action("cancel navigation", null);
        npc.action("cancel navigation due to " + event.getCancelReason().toString(), null);
    }

    // <--[event]
    // @Events
    // npc stuck
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

        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());

        Map<String, dObject> context = new HashMap<String, dObject>();

        context.put("action", new Element(event.getAction() == TeleportStuckAction.INSTANCE ? "teleport": "none"));

        // Do world script event 'On NPC stuck'
        if (NPCNavigationSmartEvent.IsActive()) {
            String determination = EventManager.doEvents(Arrays.asList
                    ("npc stuck"), npc, null, context);
            if (determination.equalsIgnoreCase("none"))
                event.setAction(null);
            if (determination.equalsIgnoreCase("teleport"))
                event.setAction(TeleportStuckAction.INSTANCE);
        }

        // Do the assignment script action
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        String determination2 = npc.action("stuck", null, context);
        if (determination2.equalsIgnoreCase("none"))
            event.setAction(null);
        if (determination2.equalsIgnoreCase("teleport"))
            event.setAction(TeleportStuckAction.INSTANCE);

    }
}
