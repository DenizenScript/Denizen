package com.denizenscript.denizen.events.core;

import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.OldSmartEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CuboidEnterExitSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    ArrayList<String> cuboids_to_watch = new ArrayList<>();

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
                    cuboids_to_watch.add(CoreUtilities.toLowerCase(m.group(1)));
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
        Debug.log("Loaded Cuboid Enter & Exit SmartEvent.");
    }


    @Override
    public void breakDown() {
        PlayerMoveEvent.getHandlerList().unregister(this);
        PlayerTeleportEvent.getHandlerList().unregister(this);
        PlayerChangedWorldEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerJoinEvent.getHandlerList().unregister(this);
        VehicleMoveEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    private boolean broad_detection = false;
    private Map<String, List<CuboidTag>> player_cuboids = new ConcurrentHashMap<>();

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
    // <context.cause> returns the cause of the event. Can be: WALK, WORLD_CHANGE, JOIN, LEAVE, TELEPORT, VEHICLE
    //
    // @Determine
    // "CANCELLED" to stop the player from moving, if cause is WALK or TELEPORT.
    //
    // -->
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        PlayerMoveEvent evt = new PlayerMoveEvent(event.getPlayer(), event.getFrom(), event.getTo());
        internalRun(evt, "teleport");
        if (evt.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        double pos = 10000000d;
        PlayerMoveEvent pme = new PlayerMoveEvent(event.getPlayer(), event.getPlayer().getLocation(),
                new Location(event.getPlayer().getWorld(), pos, pos, pos));
        internalRun(pme, "leave");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        double pos = 10000000d;
        PlayerMoveEvent pme = new PlayerMoveEvent(event.getPlayer(),
                new Location(event.getPlayer().getWorld(), pos, pos, pos), event.getPlayer().getLocation());
        internalRun(pme, "join");
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        Location to = event.getPlayer().getLocation().clone();
        Location from = event.getPlayer().getLocation().clone();
        from.setWorld(event.getFrom());
        PlayerMoveEvent evt = new PlayerMoveEvent(event.getPlayer(), from, to);
        internalRun(evt, "world_change");
    }

    @EventHandler
    public void vehicleMoveEvent(VehicleMoveEvent event) {
        for (Entity entity : event.getVehicle().getPassengers()) {
            if (EntityTag.isPlayer(entity)) {
                PlayerMoveEvent evt = new PlayerMoveEvent((Player) entity, event.getFrom(), event.getTo());
                internalRun(evt, "vehicle");
            }
        }
    }

    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent event) {
        internalRun(event, "walk");
    }

    public void internalRun(PlayerMoveEvent event, String cause) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }

        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        // Look for cuboids that contain the block's location
        List<CuboidTag> cuboids = CuboidTag.getNotableCuboidsContaining(event.getTo());
        List<CuboidTag> match = new ArrayList<>();
        String namelow = CoreUtilities.toLowerCase(event.getPlayer().getName()); // TODO: UUID?
        if (player_cuboids.containsKey(namelow)) // TODO: Clear on quit?
        {
            match = player_cuboids.get(namelow);
        }

        List<CuboidTag> exits = new ArrayList<>(match);
        exits.removeAll(cuboids);

        List<CuboidTag> enters = new ArrayList<>(cuboids);
        enters.removeAll(match);

        if (exits.isEmpty() && enters.isEmpty()) {
            return;
        }

        if (!exits.isEmpty()) {
            if (broad_detection) {
                ListTag cuboid_context = new ListTag();
                for (CuboidTag cuboid : exits) {
                    cuboid_context.add(cuboid.identify());
                }
                if (Fire(event, cuboid_context, "player exits notable cuboid", cause)) {
                    return;
                }
            }
            for (CuboidTag cuboid : exits) {
                if (Fire(event, new ListTag(cuboid.identify()), "player exits " + cuboid.identifySimple(), cause)) {
                    return;
                }
            }
        }

        if (!enters.isEmpty()) {
            if (broad_detection) {
                ListTag cuboid_context = new ListTag();
                for (CuboidTag cuboid : enters) {
                    cuboid_context.add(cuboid.identify());
                }
                if (Fire(event, cuboid_context, "player enters notable cuboid", cause)) {
                    return;
                }
            }
            for (CuboidTag cuboid : enters) {
                if (Fire(event, new ListTag(cuboid.identify()), "player enters " + cuboid.identifySimple(), cause)) {
                    return;
                }
            }
        }

        player_cuboids.put(namelow, cuboids);
    }

    /**
     * Fires world events for the Cuboid Enter/Exit Smart Event.
     */
    private boolean Fire(PlayerMoveEvent event, ListTag cuboids, String EventName, String cause) {
        List<String> events = new ArrayList<>();
        Map<String, ObjectTag> context = new HashMap<>();
        context.put("from", new LocationTag(event.getFrom()));
        context.put("to", new LocationTag(event.getTo()));
        context.put("cuboids", cuboids);
        context.put("cause", new ElementTag(cause));
        events.add(EventName);

        String determination = BukkitWorldScriptHelper.doEvents(events,
                null, EntityTag.getPlayerFrom(event.getPlayer()), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED")) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }
}
