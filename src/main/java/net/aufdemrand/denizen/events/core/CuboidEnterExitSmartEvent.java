package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CuboidEnterExitSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    ArrayList<String> cuboids_to_watch = new ArrayList<String>();

    @Override
    public boolean shouldInitialize(Set<String> events) {
        boolean should_register = false;

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on player (?:enters|exits) (notable cuboid|(cu@)?\\w+)", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {

                // We'll actually check the content supplied -- this is a SMART event after all!
                // If registerable, the event matched our checks, and this event can be safely loaded.
                boolean registerable = true;

                // Check if this event is for a cuboid who's name is not known at script time
                if (m.group(1).equalsIgnoreCase("notable cuboid")) {
                    broad_detection = true;
                }
                // Check first group which contains entity name against dCuboid's matches() method
                else if (!dCuboid.matches(m.group(1))) {
                    dB.echoError("Possible issue with '" + event + "' world event in script(s) " + EventManager.events.get(event)
                            + ". Specified cuboid is not valid.");
                    registerable = false;
                } else {
                    cuboids_to_watch.add(m.group(1).toLowerCase());
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
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Cuboid Enter & Exit SmartEvent.");
    }


    @Override
    public void breakDown() {
        PlayerMoveEvent.getHandlerList().unregister(this);
    }



    //////////////
    //  MECHANICS
    ///////////

    // TODO: Actually use these variables?
    private boolean broad_detection = false;
    private Map<String, List<dCuboid>> player_cuboids = new ConcurrentHashMap<String, List<dCuboid>>();

    // <--[event]
    // @Events
    // player enters <notable cuboid>
    // player exits <notable cuboid>
    // player enters notable cuboid
    // player exits notable cuboid
    //
    // @Regex on player (?:enters|exits) (notable cuboid|(cu@)?\w+)
    //
    // @Triggers when a player enters or exits a notable cuboid.
    // @Context
    // <context.from> returns the block location moved from.
    // <context.to> returns the block location moved to.
    //
    // -->
    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent event) {

        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        // Initialize events and context
        List<String> events = new ArrayList<String>();
        Map<String, dObject> context = new HashMap<String, dObject>();

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getTo());
        List<dCuboid> match = new ArrayList<dCuboid>();
        if (player_cuboids.containsKey(event.getPlayer().getName().toLowerCase()))
            match = player_cuboids.get(event.getPlayer().getName().toLowerCase());

        List<dCuboid> exits = new ArrayList<dCuboid>(match);
        exits.removeAll(cuboids);

        List<dCuboid> enters = new ArrayList<dCuboid>(cuboids);
        enters.removeAll(match);

        if (exits.isEmpty() && enters.isEmpty()) return;

        if (!exits.isEmpty())
            for (dCuboid cuboid : exits)
                events.add("player exits " + cuboid.identifySimple());

        if (!enters.isEmpty())
            for (dCuboid cuboid : enters)
                events.add("player enters " + cuboid.identifySimple());

        String determination = EventManager.doEvents(events,
                null, event.getPlayer(), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);

        player_cuboids.put(event.getPlayer().getName().toLowerCase(), cuboids);
    }



}
