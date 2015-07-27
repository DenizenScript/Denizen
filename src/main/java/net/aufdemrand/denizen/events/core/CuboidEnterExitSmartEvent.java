package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CuboidEnterExitSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    ArrayList<String> cuboids_to_watch = new ArrayList<String>();

    @Override
    public boolean shouldInitialize(Set<String> events) {
        boolean should_register = false;
        cuboids_to_watch.clear();

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            // TODO: Require cu@ to prevent conflict with similar events? Or restore cuboid-name-confirmation after notable cuboids save properly.
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
                else {
                    // Add this to the watch list, regardless of whether it's valid, in case it becomes valid later
                    cuboids_to_watch.add(m.group(1).toLowerCase());
                }

                // We'll set should_register to true, but keep iterating through the matches
                // to check them for errors and add to the watchlist.
                should_register = true;
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
        PlayerTeleportEvent.getHandlerList().unregister(this);
        PlayerChangedWorldEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    private boolean broad_detection = false;
    private Map<String, List<dCuboid>> player_cuboids = new ConcurrentHashMap<String, List<dCuboid>>();

    // <--[event]
    // @Events
    // player enters <notable cuboid>
    // player exits <notable cuboid>
    // player enters notable cuboid
    // player exits notable cuboid
    //
    // @Regex ^on player (enters|exits) (notable cuboid|[^\s]+)$
    //
    // @Triggers when a player enters or exits a notable cuboid.
    // @Context
    // <context.from> returns the block location moved from.
    // <context.to> returns the block location moved to.
    // <context.cuboids> returns a list of cuboids entered/exited (when no cuboid is specified in the event name).
    //
    // @Determine
    // "CANCELLED" to stop the player from moving.
    //
    //
    // -->
    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        PlayerMoveEvent evt = new PlayerMoveEvent(event.getPlayer(), event.getFrom(), event.getTo());
        playerMoveEvent(evt);
        if (evt.isCancelled())
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        double pos = 10000000d;
        PlayerMoveEvent pme = new PlayerMoveEvent(event.getPlayer(), event.getPlayer().getLocation(),
                new Location(event.getPlayer().getWorld(), pos, pos, pos));
        playerMoveEvent(pme);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        double pos = 10000000d;
        PlayerMoveEvent pme = new PlayerMoveEvent(event.getPlayer(),
                new Location(event.getPlayer().getWorld(), pos, pos, pos), event.getPlayer().getLocation());
        playerMoveEvent(pme);
    }

    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        Location to = event.getPlayer().getLocation().clone();
        Location from = event.getPlayer().getLocation().clone();
        from.setWorld(event.getFrom());
        PlayerMoveEvent evt = new PlayerMoveEvent(event.getPlayer(), from, to);
        playerMoveEvent(evt);
    }

    @EventHandler(ignoreCancelled = true)
    public void playerMoveEvent(PlayerMoveEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }

        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getTo());
        List<dCuboid> match = new ArrayList<dCuboid>();
        String namelow = CoreUtilities.toLowerCase(event.getPlayer().getName()); // TODO: UUID?
        if (player_cuboids.containsKey(namelow)) // TODO: Clear on quit?
            match = player_cuboids.get(namelow);

        List<dCuboid> exits = new ArrayList<dCuboid>(match);
        exits.removeAll(cuboids);

        List<dCuboid> enters = new ArrayList<dCuboid>(cuboids);
        enters.removeAll(match);

        if (exits.isEmpty() && enters.isEmpty()) return;

        if (!exits.isEmpty()) {
            if (broad_detection) {
                dList cuboid_context = new dList();
                for (dCuboid cuboid : exits) {
                    cuboid_context.add(cuboid.identify());
                }
                if (Fire(event, cuboid_context, "player exits notable cuboid"))
                    return;
            }
            for (dCuboid cuboid : exits) {
                if (Fire(event, new dList(cuboid.identify()), "player exits " + cuboid.identifySimple()))
                    return;
            }
        }

        if (!enters.isEmpty()) {
            if (broad_detection) {
                dList cuboid_context = new dList();
                for (dCuboid cuboid : enters) {
                    cuboid_context.add(cuboid.identify());
                }
                if (Fire(event, cuboid_context, "player enters notable cuboid"))
                    return;
            }
            for (dCuboid cuboid : enters) {
                if (Fire(event, new dList(cuboid.identify()), "player enters " + cuboid.identifySimple()))
                    return;
            }
        }

        player_cuboids.put(namelow, cuboids);
    }

    /**
     * Fires world events for the Cuboid Enter/Exit Smart Event.
     */
    private boolean Fire(PlayerMoveEvent event, dList cuboids, String EventName) {
        List<String> events = new ArrayList<String>();
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("from", new dLocation(event.getFrom()));
        context.put("to", new dLocation(event.getTo()));
        context.put("cuboids", cuboids);
        events.add(EventName);

        String determination = BukkitWorldScriptHelper.doEvents(events,
                null, dEntity.getPlayerFrom(event.getPlayer()), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED")) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }
}
