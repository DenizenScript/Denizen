package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.*;

public class AreaEnterExitScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player enters <area>
    // player exits <area>
    // player enters/exits cuboid
    // player enters/exits ellipsoid
    //
    // @Regex ^on player (enters|exits) [^\s]+$
    //
    // @Group Player
    //
    // @Triggers when a player enters or exits a noted area (cuboid or ellipsoid).
    //
    // @Warning cancelling this event will have different results depending on the cause. Teleporting the player away 1 tick later might be safer.
    //
    // @Cancellable true
    //
    // @Context
    // <context.area> returns the area object that was entered or exited.
    // <context.cause> returns the cause of the event. Can be: WALK, WORLD_CHANGE, JOIN, QUIT, TELEPORT, VEHICLE.
    // <context.to> returns the location the player moved to.
    // <context.from> returns the location the player moved from (when available).
    //
    // @Player Always.
    //
    // -->

    public AreaEnterExitScriptEvent() {
        instance = this;
    }

    public static AreaEnterExitScriptEvent instance;

    public PlayerTag currentPlayer;
    public AreaContainmentObject area;
    public boolean isEntering;
    public Location to;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player enters") && !path.eventLower.startsWith("player exits")) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("biome") || exactMatchesEnum(path.eventArgLowerAt(2), Biome.values())) {
            return false;
        }
        if (exactMatchesVehicle(path.eventArgLowerAt(2))) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("bed") || path.eventArgLowerAt(2).equals("portal")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (isEntering && !path.eventArgLowerAt(1).equals("enters")) {
            return false;
        }
        if (!isEntering && !path.eventArgLowerAt(1).equals("exits")) {
            return false;
        }
        String areaName = path.eventArgLowerAt(2);
        if (areaName.equals("notable")) {
            areaName = path.eventArgLowerAt(3);
        }
        if (areaName.equals("cuboid")) {
            if (!(area instanceof CuboidTag)) {
                return false;
            }
        }
        else if (areaName.equals("ellipsoid")) {
            if (!(area instanceof EllipsoidTag)) {
                return false;
            }
        }
        else {
            if (!runGenericCheck(areaName, area.getNoteName())) {
                return false;
            }
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "AreaEnterExit";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(currentPlayer, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("area")) {
            return area;
        }
        else if (name.equals("cause")) {
            String cause;
            if (currentEvent instanceof PlayerJoinEvent) {
                cause = "JOIN";
            }
            else if (currentEvent instanceof PlayerQuitEvent) {
                cause = "QUIT";
            }
            else if (currentEvent instanceof PlayerChangedWorldEvent) {
                cause = "WORLD_CHANGE";
            }
            else if (currentEvent instanceof PlayerTeleportEvent) {
                cause = "TELEPORT";
            }
            else if (currentEvent instanceof VehicleMoveEvent) {
                cause = "VEHICLE";
            }
            else if (currentEvent instanceof PlayerMoveEvent) {
                cause = "WALK";
            }
            else {
                cause = "UNKNOWN";
            }
            return new ElementTag(cause);
        }
        else if (name.equals("to")) {
            return new LocationTag(to);
        }
        else if (name.equals("from")) {
            if (currentEvent instanceof PlayerMoveEvent) {
                return new LocationTag(((PlayerMoveEvent) currentEvent).getFrom());
            }
            else if (currentEvent instanceof VehicleMoveEvent) {
                return new LocationTag(((VehicleMoveEvent) currentEvent).getFrom());
            }
            else {
                return new LocationTag(currentPlayer.getLocation());
            }
        }
        return super.getContext(name);
    }

    @Override
    public void init() {
        super.init();
        doTrackAll = false;
        boolean needsMatchers = false;
        List<String> exacts = new ArrayList<>();
        List<MatchHelper> matchList = new ArrayList<>();
        for (ScriptPath path : eventPaths) {
            String area = path.eventArgLowerAt(2);
            if (area.equals("notable")) {
                area = path.eventArgLowerAt(3);
            }
            if (area.equals("cuboid")) {
                doTrackAll = true;
                break;
            }
            else if (area.equals("ellipsoid")) {
                doTrackAll = true;
                break;
            }
            MatchHelper matcher = createMatcher(area);
            if (matcher instanceof AlwaysMatchHelper) {
                doTrackAll = true;
                break;
            }
            else if (!needsMatchers && (matcher instanceof ExactMatchHelper)) {
                exacts.add(area);
            }
            else {
                needsMatchers = true;
            }
            matchList.add(matcher);
        }
        exactTracked = needsMatchers ? null : exacts.toArray(new String[0]);
        matchers = needsMatchers ? matchList.toArray(new MatchHelper[0]) : null;
    }

    public boolean doTrackAll = false;
    public String[] exactTracked = null;
    public MatchHelper[] matchers = null;
    public static HashMap<UUID, HashSet<String>> playersInArea = new HashMap<>();

    @Override
    public void cancellationChanged() {
        if (cancelled) {
            HashSet<String> inAreas = playersInArea.get(currentPlayer.getOfflinePlayer().getUniqueId());
            if (isEntering) {
                inAreas.remove(CoreUtilities.toLowerCase(area.getNoteName()));
            }
            else {
                inAreas.add(CoreUtilities.toLowerCase(area.getNoteName()));
            }
        }
        super.cancellationChanged();
    }

    public boolean anyMatch(String name) {
        if (doTrackAll) {
            return true;
        }
        for (MatchHelper matcher : matchers) {
            if (matcher.doesMatch(name)) {
                return true;
            }
        }
        return false;
    }

    public void processSingle(AreaContainmentObject obj, Player player, HashSet<String> inAreas, Location pos, Event eventCause) {
        boolean containedNow = obj.doesContainLocation(pos);
        boolean wasContained = inAreas != null && inAreas.contains(obj.getNoteName());
        if (containedNow == wasContained) {
            return;
        }
        if (inAreas == null) {
            inAreas = new HashSet<>();
            playersInArea.put(player.getUniqueId(), inAreas);
        }
        if (containedNow) {
            inAreas.add(obj.getNoteName());
        }
        else {
            inAreas.remove(obj.getNoteName());
        }
        currentPlayer = new PlayerTag(player);
        isEntering = containedNow;
        area = obj;
        to = pos;
        fire(eventCause);
    }

    public void processNewPosition(Player player, Location pos, Event eventCause) {
        if (EntityTag.isNPC(player)) {
            return;
        }
        HashSet<String> inAreas = playersInArea.get(player.getUniqueId());
        if (doTrackAll || matchers != null) {
            for (CuboidTag cuboid : NotableManager.getAllType(CuboidTag.class)) {
                if (anyMatch(cuboid.noteName)) {
                    processSingle(cuboid, player, inAreas, pos, eventCause);
                }
            }
            for (EllipsoidTag ellipsoid : NotableManager.getAllType(EllipsoidTag.class)) {
                if (anyMatch(ellipsoid.noteName)) {
                    processSingle(ellipsoid, player, inAreas, pos, eventCause);
                }
            }
        }
        else {
            for (String name : exactTracked) {
                Notable obj = NotableManager.getSavedObject(name);
                if (!(obj instanceof AreaContainmentObject)) {
                    Debug.echoError("Invalid area enter/exit event area '" + name + "'");
                    continue;
                }
                processSingle((AreaContainmentObject) obj, player, inAreas, pos, eventCause);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        processNewPosition(event.getPlayer(), new Location(event.getPlayer().getWorld(), 10000000d, 10000000d, 10000000d), event);
        playersInArea.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        processNewPosition(event.getPlayer(), event.getPlayer().getLocation(), event);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (LocationTag.isSameBlock(event.getFrom(), event.getTo())) {
            return;
        }
        processNewPosition(event.getPlayer(), event.getTo(), event);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        processNewPosition(event.getPlayer(), event.getTo(), event);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        processNewPosition(event.getPlayer(), event.getPlayer().getLocation(), event);
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (LocationTag.isSameBlock(event.getFrom(), event.getTo())) {
            return;
        }
        for (Entity entity : event.getVehicle().getPassengers()) {
            if (EntityTag.isPlayer(entity)) {
                processNewPosition((Player) entity, event.getTo(), event);
            }
        }
    }
}
