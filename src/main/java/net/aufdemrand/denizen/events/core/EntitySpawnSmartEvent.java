package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntitySpawnSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {
        boolean should_register = false;

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            // TODO: Cleaner regex?
            Matcher m = Pattern.compile("on (.+|entity|npc) spawns(?: because (\\w+))?", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {

                // We'll actually check the content supplied -- this is a SMART event after all!
                // If registerable, the event matched our checks, and this event can be safely loaded.
                boolean registerable = true;

                // Check first group which contains entity name against dEntity's matches() method
                if (!dEntity.matches(m.group(1)) && (!m.group(1).equalsIgnoreCase("entity") && !m.group(1).equalsIgnoreCase("npc"))) {
                    dB.echoError("Possible issue with '" + event + "' world event in script(s) " + EventManager.events.get(event)
                            + ". Specified entity is not valid.");
                    registerable = false;
                }

                // Check second group for valid reason
                if (m.group(2) != null) {
                    if (CreatureSpawnEvent.SpawnReason.valueOf(m.group(2).toUpperCase()) == null) {
                        dB.echoError("Possible issue with '" + event + "' world event in script(s) " + EventManager.events.get(event)
                                + ". Specified reason is not valid.");
                        registerable = false;
                    }
                }

                // If registerable, we'll set should_register to true, but keep iterating through the matches
                // to check them for errors, as caught above.
                if (registerable) should_register = true;
            }
        }

        return should_register;

    }


    @Override
    public void _initialize() {
        // Yipee! Register this class with Bukkit's EventListener
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Entity Spawn SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Deregister CreatureSpawnEvent's ties to this SmartEvent
        CreatureSpawnEvent.getHandlerList().unregister(this);
    }



    //////////////
    //  MECHANICS
    ///////////


    // <--[event]
    // @Events
    // entity spawns
    // entity spawns in <notable cuboid>
    // entity spawns because <cause>
    // entity spawns in <notable cuboid> because <cause>
    // <entity> spawns
    // <entity> spawns in <notable cuboid>
    // <entity> spawns because <cause>
    // <entity> spawns in <notable cuboid> because <cause>
    //
    // @Regex on (.+|entity|npc) spawns(?: because (\w+))?
    //
    // @Warning This event may fire very rapidly.
    //
    // @Triggers when an entity spawns.
    // @Context
    // <context.entity> returns the dEntity that spawned.
    // <npc> if the entity spawned is a NPC.
    // <context.reason> returns the reason the entity spawned.
    // <context.location> returns the location the entity will spawn at.
    // <context.cuboids> returns a list of cuboids that the entity spawned inside.
    //
    // @Determine
    // "CANCELLED" to stop the entity from spawning.
    //
    // -->
    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent event) {

        List<String> events = new ArrayList<String>();
        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        String reason = event.getSpawnReason().name();

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getLocation());

        dList cuboid_context = new dList();
        for (dCuboid cuboid : cuboids) {
            events.add("entity spawns in " + cuboid.identifySimple());
            events.add("entity spawns in " + cuboid.identifySimple() + " because " + reason);
            events.add(entity.identifyType() + " spawns in " + cuboid.identifySimple());
            events.add(entity.identifyType() + " spawns in " + cuboid.identifySimple() + " because " + reason);
        }
        // Add in cuboids context, with either the cuboids or an empty list
        context.put("cuboids", cuboid_context);

        // Add events to fire
        events.add("entity spawns");
        events.add("entity spawns because " + reason);
        events.add(entity.identifyType() + " spawns");
        events.add(entity.identifyType() + " spawns because " + reason);

        // Add in other contexts associated with this event
        context.put("entity", entity);
        context.put("reason", new Element(reason));
        context.put("location", new dLocation(event.getLocation()));

        String determination = EventManager.doEvents(events,
                (entity.isNPC() ? entity.getDenizenNPC() : null), null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }



}
