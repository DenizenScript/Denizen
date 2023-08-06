package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.NotedAreaTracker;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.*;

public class AreaEnterExitScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> enters|exits <area>
    //
    // @Group Entity
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
    // <context.to> returns the location the entity moved to (might not be available in exit events).
    // <context.from> returns the location the entity moved from (when available, depending on cause).
    // <context.entity> returns the entity that entered/exited an area.
    //
    // @Player When the entity is a player.
    //
    // -->

    public AreaEnterExitScriptEvent() {
        registerCouldMatcher("<entity> enters|exits <area>");
    }


    public EntityTag currentEntity;
    public AreaContainmentObject area;
    public boolean isEntering;
    public Location to;

    @Override
    public boolean matches(ScriptPath path) {
        if (isEntering && !path.eventArgLowerAt(1).equals("enters")) {
            return false;
        }
        if (!isEntering && !path.eventArgLowerAt(1).equals("exits")) {
            return false;
        }
        String areaName = path.eventArgLowerAt(2);
        if (areaName.equals("notable")) { // TODO: Deprecate?
            areaName = path.eventArgLowerAt(3);
        }
        if (!area.tryAdvancedMatcher(areaName)) {
            return false;
        }
        if (!path.tryArgObject(0, currentEntity)) {
            return false;
        }
        return super.matches(path);
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
        else if (name.equals("to") && to != null) {
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
        doTrackAll = false;
        boolean needsMatchers = false;
        HashSet<String> exacts = new HashSet<>();
        List<MatchHelper> matchList = new ArrayList<>();
        HashSet<String> flags = new HashSet<>();
        onlyTrackPlayers = true;
        for (ScriptPath path : eventPaths) {
            if (!path.eventArgLowerAt(0).equals("player")) {
                onlyTrackPlayers = false;
            }
            String area = path.eventArgLowerAt(2);
            if (area.equals("notable")) {
                area = path.eventArgLowerAt(3);
            }
            if (area.equals("cuboid") || area.equals("ellipsoid") || area.equals("polygon")) {
                doTrackAll = true;
                needsMatchers = true;
            }
            MatchHelper matcher = createMatcher(area);
            if (matcher instanceof AlwaysMatchHelper) {
                doTrackAll = true;
                needsMatchers = true;
            }
            else if (!needsMatchers && (matcher instanceof ExactMatchHelper)) {
                exacts.add(area);
            }
            else {
                needsMatchers = true;
            }
            matchList.add(matcher);
            if (area.startsWith("area_flagged:")) {
                flags.add(CoreUtilities.toLowerCase(area.substring("area_flagged:".length())));
                needsMatchers = true;
            }
        }
        exactTracked = needsMatchers ? null : exacts.toArray(new String[0]);
        matchers = needsMatchers ? matchList.toArray(new MatchHelper[0]) : null;
        flagTracked = flags.size() > 0 ? flags.toArray(new String[0]) : null;
        registerCorrectClass();
    }

    public boolean doTrackAll = false;
    public String[] exactTracked = null;
    public String[] flagTracked = null;
    public MatchHelper[] matchers = null;
    public boolean onlyTrackPlayers = true;
    public static HashMap<UUID, HashSet<AreaContainmentObject>> entitiesInArea = new HashMap<>();

    @Override
    public void cancellationChanged() {
        if (cancelled) {
            HashSet<AreaContainmentObject> inAreas = entitiesInArea.get(currentEntity.getUUID());
            if (isEntering) {
                inAreas.remove(area);
            }
            else {
                inAreas.add(area);
            }
        }
        super.cancellationChanged();
    }

    public boolean anyMatch(String name, FlaggableObject flaggable) {
        if (doTrackAll) {
            return true;
        }
        if (matchers != null) {
            for (MatchHelper matcher : matchers) {
                if (matcher.doesMatch(name)) {
                    return true;
                }
            }
        }
        if (flagTracked != null) {
            for (String flag : flagTracked) {
                AbstractFlagTracker tracker = flaggable.getFlagTracker();
                if (tracker != null && tracker.hasFlag(flag)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void processSingle(AreaContainmentObject obj, EntityTag entity, HashSet<AreaContainmentObject> inAreas, Location pos, Event eventCause) {
        boolean containedNow = pos != null && obj.doesContainLocation(pos);
        boolean wasContained = inAreas != null && inAreas.contains(obj);
        if (containedNow == wasContained) {
            return;
        }
        if (inAreas == null) {
            inAreas = new HashSet<>();
            entitiesInArea.put(entity.getUUID(), inAreas);
        }
        if (containedNow) {
            inAreas.add(obj);
        }
        else {
            inAreas.remove(obj);
        }
        currentEntity = entity;
        isEntering = containedNow;
        area = obj;
        to = pos;
        fire(eventCause);
    }

    private final static List<AreaContainmentObject> reusableClearList = new ArrayList<>();

    public void processNewPosition(EntityTag entity, Location pos, Event eventCause) {
        if (onlyTrackPlayers && !entity.isPlayer()) {
            return;
        }
        HashSet<AreaContainmentObject> inAreas = entitiesInArea.get(entity.getUUID());
        if (doTrackAll || matchers != null || flagTracked != null) {
            if (pos != null) {
                NotedAreaTracker.forEachAreaThatContains(new LocationTag(pos), (a) -> {
                    if (a instanceof FlaggableObject && anyMatch(a.getNoteName(), (FlaggableObject) a)) {
                        processSingle(a, entity, inAreas, pos, eventCause);
                    }
                });
            }
            if (inAreas != null) {
                reusableClearList.addAll(inAreas);
                for (AreaContainmentObject area : reusableClearList) {
                    if (area.getNoteName() == null) {
                        inAreas.remove(area);
                    }
                    else {
                        processSingle(area, entity, inAreas, pos, eventCause);
                    }
                }
                reusableClearList.clear();
            }
        }
        else {
            for (String name : exactTracked) {
                Notable obj = NoteManager.getSavedObject(name);
                if (!(obj instanceof AreaContainmentObject)) {
                    Debug.echoError("Invalid area enter/exit event area '" + name + "'");
                    continue;
                }
                processSingle((AreaContainmentObject) obj, entity, inAreas, pos, eventCause);
            }
        }
        if (inAreas != null && inAreas.isEmpty()) {
            entitiesInArea.remove(entity.getUUID());
        }
    }

    public class SpigotListeners implements Listener {

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            processNewPosition(new EntityTag(event.getPlayer()), null, event);
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
        public void onTeleport(EntityTeleportEvent event) {
            if (!onlyTrackPlayers) {
                processNewPosition(new EntityTag(event.getEntity()), event.getTo(), event);
            }
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
            if (!onlyTrackPlayers) {
                processNewPosition(new EntityTag(event.getVehicle()), event.getTo(), event);
            }
            for (Entity entity : event.getVehicle().getPassengers()) {
                if (!onlyTrackPlayers || EntityTag.isPlayer(entity)) {
                    processNewPosition(new EntityTag(entity), event.getTo(), event);
                }
            }
        }
    }
}
