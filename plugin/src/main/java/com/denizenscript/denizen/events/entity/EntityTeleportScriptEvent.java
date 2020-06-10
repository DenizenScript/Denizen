package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EntityTeleportScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity teleports
    // <entity> teleports
    //
    // @Regex ^on [^\s]+ teleports$
    //
    // @Group Entity
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    // @Switch cause:<cause> to only process the event when it came from a specified cause.
    //
    // @Triggers when an entity teleports.
    //
    // @Cancellable true
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.origin> returns the LocationTag the entity teleported from.
    // <context.destination> returns the LocationTag the entity teleported to.
    // <context.cause> returns an ElementTag of the teleport cause. Can be:
    // COMMAND, END_PORTAL, ENDER_PEARL, NETHER_PORTAL, PLUGIN, END_GATEWAY, CHORUS_FRUIT, SPECTATE, UNKNOWN, or ENTITY_TELEPORT
    //
    // @Determine
    // "ORIGIN:" + LocationTag to change the location the entity teleported from.
    // "DESTINATION:" + LocationTag to change the location the entity teleports to.
    //
    // @Player when the entity being teleported is a player.
    //
    // @NPC when the entity being teleported is an NPC.
    //
    // -->

    public EntityTeleportScriptEvent() {
        instance = this;
    }

    public static EntityTeleportScriptEvent instance;
    public EntityTag entity;
    public LocationTag from;
    public LocationTag to;
    public String cause;
    public EntityTeleportEvent event;
    public PlayerTeleportEvent pEvent;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("teleports")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        if (!runGenericSwitchCheck(path, "cause", cause)) {
            return false;
        }

        if (!runInCheck(path, from)) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public String getName() {
        return "EntityTeleports";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        String dlow = CoreUtilities.toLowerCase(determination);
        if (dlow.startsWith("origin:")) {
            LocationTag new_from = LocationTag.valueOf(determination.substring("origin:".length()));
            if (new_from != null) {
                from = new_from;
                if (event != null) {
                    event.setFrom(new_from);
                }
                else {
                    pEvent.setFrom(new_from);
                }
                return true;
            }
        }
        else if (dlow.startsWith("destination:")) {
            LocationTag new_to = LocationTag.valueOf(determination.substring("destination:".length()));
            if (new_to != null) {
                to = new_to;
                if (event != null) {
                    event.setTo(new_to);
                }
                else {
                    pEvent.setTo(new_to);
                }
                return true;
            }
        }
        else if (LocationTag.matches(determination)) {
            LocationTag new_to = LocationTag.valueOf(determination);
            if (new_to != null) {
                to = new_to;
                if (event != null) {
                    event.setTo(new_to);
                }
                else {
                    pEvent.setTo(new_to);
                }
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("origin")) {
            return from;
        }
        else if (name.equals("destination")) {
            return to;
        }
        else if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("cause")) {
            return new ElementTag(cause);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityTeleports(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        to = new LocationTag(event.getTo());
        from = new LocationTag(event.getFrom());
        entity = new EntityTag(event.getEntity());
        cause = "ENTITY_TELEPORT";
        this.event = event;
        pEvent = null;
        fire(event);
    }

    @EventHandler
    public void onPlayerTeleports(PlayerTeleportEvent event) {
        from = new LocationTag(event.getFrom());
        to = new LocationTag(event.getTo());
        entity = new EntityTag(event.getPlayer());
        cause = event.getCause().name();
        this.event = null;
        pEvent = event;
        fire(event);
    }
}
