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
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.*;

public class AreaEnterExitScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity enters/exits cuboid/ellipsoid/polygon
    // <entity> enters <area>
    // <entity> exits <area>
    //
    // @Regex ^on [^\s]+ (enters|exits) [^\s]+$
    //
    // @Group Player
    //
    // @Triggers when an entity enters or exits a noted area (cuboid, ellipsoid, or polygon). On Spigot servers, only fires for players. Paper is required for other mob types.
    //
    // @Warning cancelling this event will have different results depending on the cause. Teleporting the entity away 1 tick later might be safer.
    //
    // @Cancellable true
    //
    // @Context
    // <context.area> returns the area object that was entered or exited.
    // <context.cause> returns the cause of the event. Can be: WALK, WORLD_CHANGE, JOIN, QUIT, TELEPORT, VEHICLE.
    // <context.to> returns the location the player moved to.
    // <context.from> returns the location the player moved from (when available).
    // <context.entity> returns the entity that entered/exited an area.
    //
    // @Player When the entity is a player.
    //
    // -->

    public AreaEnterExitScriptEvent() {
        instance = this;
    }

    public static AreaEnterExitScriptEvent instance;

    public EntityTag currentEntity;
    public AreaContainmentObject area;
    public boolean isEntering;
    public Location to;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("enters") && !path.eventArgLowerAt(1).equals("exits")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("biome") || exactMatchesEnum(path.eventArgLowerAt(2), Biome.values())) {
            return false;
        }
        if (exactMatchEntity(path.eventArgLowerAt(2))) {
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
        return new BukkitScriptEntryData(currentEntity);
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
                return new LocationTag(currentEntity.getLocation());
            }
        }
        else if (name.equals("entity")) {
            return currentEntity.getDenizenObject();
        }
        return super.getContext(name);
    }

    public void registerCorrectClass() {
        initListener(new SpigotListeners());
    }

    @Override
    public void init() {
        registerCorrectClass();
        doTrackAll = false;
        boolean needsMatchers = false;
        HashSet<String> exacts = new HashSet<>();
        List<MatchHelper> matchList = new ArrayList<>();
        onlyTrackPlayers = true;
        for (ScriptPath path : eventPaths) {
            if (!path.eventArgLowerAt(0).equals("player")) {
                onlyTrackPlayers = false;
            }
            String area = path.eventArgLowerAt(2);
            if (area.equals("notable")) {
                area = path.eventArgLowerAt(3);
            }
            if (area.equals("cuboid")) {
                doTrackAll = true;
            }
            else if (area.equals("ellipsoid")) {
                doTrackAll = true;
            }
            MatchHelper matcher = createMatcher(area);
            if (matcher instanceof AlwaysMatchHelper) {
                doTrackAll = true;
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
    public boolean onlyTrackPlayers = true;
    public static HashMap<UUID, HashSet<String>> entitiesInArea = new HashMap<>();

    @Override
    public void cancellationChanged() {
        if (cancelled) {
            HashSet<String> inAreas = entitiesInArea.get(currentEntity.getUUID());
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

    public void processSingle(AreaContainmentObject obj, EntityTag entity, HashSet<String> inAreas, Location pos, Event eventCause) {
        boolean containedNow = obj.doesContainLocation(pos);
        boolean wasContained = inAreas != null && inAreas.contains(obj.getNoteName());
        if (containedNow == wasContained) {
            return;
        }
        if (containedNow) {
            inAreas.add(obj.getNoteName());
        }
        else {
            inAreas.remove(obj.getNoteName());
        }
        currentEntity = entity;
        isEntering = containedNow;
        area = obj;
        to = pos;
        fire(eventCause);
    }

    public void processNewPosition(EntityTag entity, Location pos, Event eventCause) {
        if (onlyTrackPlayers && !entity.isPlayer()) {
            return;
        }
        HashSet<String> inAreas = entitiesInArea.get(entity.getUUID());
        if (inAreas == null) {
            inAreas = new HashSet<>();
            entitiesInArea.put(entity.getUUID(), inAreas);
        }
        if (doTrackAll || matchers != null) {
            for (CuboidTag cuboid : NotableManager.getAllType(CuboidTag.class)) {
                if (anyMatch(cuboid.noteName)) {
                    processSingle(cuboid, entity, inAreas, pos, eventCause);
                }
            }
            for (EllipsoidTag ellipsoid : NotableManager.getAllType(EllipsoidTag.class)) {
                if (anyMatch(ellipsoid.noteName)) {
                    processSingle(ellipsoid, entity, inAreas, pos, eventCause);
                }
            }
            for (PolygonTag polygon : NotableManager.getAllType(PolygonTag.class)) {
                if (anyMatch(polygon.noteName)) {
                    processSingle(polygon, entity, inAreas, pos, eventCause);
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
                processSingle((AreaContainmentObject) obj, entity, inAreas, pos, eventCause);
            }
        }
    }

    public class SpigotListeners implements Listener {

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            processNewPosition(new EntityTag(event.getPlayer()), new Location(event.getPlayer().getWorld(), 10000000d, 10000000d, 10000000d), event);
            entitiesInArea.remove(event.getPlayer().getUniqueId());
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            processNewPosition(new EntityTag(event.getPlayer()), event.getPlayer().getLocation(), event);
        }

        @EventHandler
        public void onMove(PlayerMoveEvent event) {
            if (LocationTag.isSameBlock(event.getFrom(), event.getTo())) {
                return;
            }
            processNewPosition(new EntityTag(event.getPlayer()), event.getTo(), event);
        }

        @EventHandler
        public void onTeleport(PlayerTeleportEvent event) {
            processNewPosition(new EntityTag(event.getPlayer()), event.getTo(), event);
        }

        @EventHandler
        public void onWorldChange(PlayerChangedWorldEvent event) {
            processNewPosition(new EntityTag(event.getPlayer()), event.getPlayer().getLocation(), event);
        }

        @EventHandler
        public void onVehicleMove(VehicleMoveEvent event) {
            if (LocationTag.isSameBlock(event.getFrom(), event.getTo())) {
                return;
            }
            for (Entity entity : event.getVehicle().getPassengers()) {
                if (!onlyTrackPlayers || EntityTag.isPlayer(entity)) {
                    processNewPosition(new EntityTag(entity), event.getTo(), event);
                }
            }
        }
    }
}
