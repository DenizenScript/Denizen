package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.events.core.NPCNavigationSmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NPCTags implements Listener {

    public NPCTags(Denizen denizen) {
        if (Depends.citizens != null)
            denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void npcTags(ReplaceableTagEvent event) {

        if (!event.matches("npc") || event.replaced()) return;

        // Build a new attribute out of the raw_tag supplied in the script to be fulfilled
        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());

        // PlayerTags require a... dPlayer!
        dNPC n = event.getNPC();

        // Player tag may specify a new player in the <player[context]...> portion of the tag.
        if (attribute.hasContext(1))
            // Check if this is a valid player and update the dPlayer object reference.
            if (dNPC.matches(attribute.getContext(1)))
                n = dNPC.valueOf(attribute.getContext(1));
            else {
                dB.echoDebug(event.getScriptEntry(), "Could not match '" + attribute.getContext(1) + "' to a valid NPC!");
                return;
            }


        if (n == null || !n.isValid()) {
            dB.echoDebug(event.getScriptEntry(), "Invalid or missing NPC for tag <" + event.raw_tag + ">!");
            event.setReplaced("null");
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

        // Do world script event 'On NPC Completes Navigation'
        if (NPCNavigationSmartEvent.IsActive())
            EventManager.doEvents(Arrays.asList
                    ("npc completes navigation"),
                    dNPC.mirrorCitizensNPC(event.getNPC()), null, null);

        // Do the assignment script action
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());
        npc.action("complete navigation", null);

    }

    // <--[event]
    // @Events
    // npc begins navigation
    //
    // @Warning This event may fire very rapidly.
    //
    // @Triggers when an NPC begins navigating.
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
        // Do world script event 'On NPC Completes Navigation'
        if (NPCNavigationSmartEvent.IsActive())
            EventManager.doEvents(Arrays.asList
                    ("npc begins navigation"),
                    dNPC.mirrorCitizensNPC(event.getNPC()), null, null);

        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());
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
        EventManager.doEvents(Arrays.asList
                ("npc cancels navigation"),
                dNPC.mirrorCitizensNPC(event.getNPC()), null, null);

        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());
        npc.action("cancel navigation", null);
        npc.action("cancel navigation due to " + event.getCancelReason().toString(), null);
    }
}
