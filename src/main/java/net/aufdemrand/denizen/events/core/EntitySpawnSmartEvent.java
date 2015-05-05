package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntitySpawnSmartEvent implements OldSmartEvent, Listener {


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
            Matcher m = Pattern.compile("on (\\w+) spawns(?: in (\\w+))?(?: because (\\w+))?", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {

                // Check first group which contains entity name against dEntity's matches() method
                if (!dEntity.matches(m.group(1)) && (!m.group(1).equalsIgnoreCase("entity") && !m.group(1).equalsIgnoreCase("npc"))) {
                    // This is where we would throw an error message... if we wanted one, which we don't anymore.
                    // (See: Item Spawns event)
                }
                else {
                    // If registerable, we'll set should_register to true, but keep iterating through the matches
                    // to check them for errors, as caught above.
                    should_register = true;
                }
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
    // entity spawns (in <notable cuboid>) (because <cause>)
    // <entity> spawns
    // <entity> spawns (in <notable cuboid>) (because <cause>)
    //
    // @Regex on (\w+) spawns(?: in (\w+))?(?: because (\w+))?
    //
    // @Warning This event may fire very rapidly.
    //
    // @Triggers when an entity spawns.
    // @Context
    // <context.entity> returns the dEntity that spawned.
    // <context.reason> returns the reason the entity spawned.
    // <context.location> returns the location the entity will spawn at.
    // <context.cuboids> returns a list of cuboids that the entity spawned inside.
    //
    // @Determine
    // "CANCELLED" to stop the entity from spawning.
    //
    // -->
    @EventHandler
    public void npcSpawn(NPCSpawnEvent event) {
        if (event.getNPC().getEntity() instanceof LivingEntity)
        creatureSpawn(new CreatureSpawnEvent((LivingEntity)event.getNPC().getEntity(),
                CreatureSpawnEvent.SpawnReason.CUSTOM));
    }

    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent event) {

        List<String> events = new ArrayList<String>();
        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        String reason = event.getSpawnReason().name();

        // Remember the entity (for adjusting and stuff before it's spawned)
        dEntity.rememberEntity(event.getEntity());

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getLocation());

        dList cuboid_context = new dList();
        for (dCuboid cuboid : cuboids) {
            events.add("entity spawns in " + cuboid.identifySimple());
            events.add("entity spawns in " + cuboid.identifySimple() + " because " + reason);
            events.add(entity.identifyType() + " spawns in " + cuboid.identifySimple());
            events.add(entity.identifyType() + " spawns in " + cuboid.identifySimple() + " because " + reason);
            events.add(entity.identifySimple() + " spawns in " + cuboid.identifySimple());
            events.add(entity.identifySimple() + " spawns in " + cuboid.identifySimple() + " because " + reason);
            cuboid_context.add(cuboid.identify());
        }
        // Add in cuboids context, with either the cuboids or an empty list
        context.put("cuboids", cuboid_context);

        // Add events to fire
        events.add("entity spawns");
        events.add("entity spawns because " + reason);
        events.add(entity.identifyType() + " spawns");
        events.add(entity.identifyType() + " spawns because " + reason);
        events.add(entity.identifySimple() + " spawns");
        events.add(entity.identifySimple() + " spawns because " + reason);

        // Add in other contexts associated with this event
        context.put("entity", entity);
        context.put("reason", new Element(reason));
        context.put("location", new dLocation(event.getLocation()));

        String determination = BukkitWorldScriptHelper.doEvents(events,
                (entity.isCitizensNPC() ? entity.getDenizenNPC() : null), null, context, true);

        dEntity.forgetEntity(event.getEntity());

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
}
