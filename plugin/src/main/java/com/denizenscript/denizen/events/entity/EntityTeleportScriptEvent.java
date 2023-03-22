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
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EntityTeleportScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[language]
    // @name Teleport Cause
    // @group Useful Lists
    // @description
    // Possible player teleport causes: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/PlayerTeleportEvent.TeleportCause.html>
    // These are used in <@link event entity teleports>, <@link tag server.teleport_causes>, <@link command teleport>, ...
    // Note that these causes will only work for player entities.
    //
    // Additionally, Denizen provides two basic teleport causes for non-player entity teleport events: ENTITY_PORTAL and ENTITY_TELEPORT.
    // These additional causes are only for <@link event entity teleports>, and thus not usable in <@link command teleport>, and will not show in <@link tag server.teleport_causes>.
    // -->

    // <--[event]
    // @Events
    // entity teleports
    // <entity> teleports
    //
    // @Regex ^on [^\s]+ teleports$
    //
    // @Group Entity
    //
    // @Location true
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
    // <context.cause> returns an ElementTag of the teleport cause - see <@link language teleport cause> for causes.
    //
    // @Determine
    // "ORIGIN:<LocationTag>" to change the location the entity teleported from.
    // "DESTINATION:<LocationTag>" to change the location the entity teleports to.
    //
    // @Player when the entity being teleported is a player.
    //
    // @NPC when the entity being teleported is an NPC.
    //
    // -->

    public EntityTeleportScriptEvent() {
    }

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
        if (!path.tryArgObject(0, entity)) {
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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        String dlow = CoreUtilities.toLowerCase(determination);
        if (dlow.startsWith("origin:")) {
            LocationTag new_from = LocationTag.valueOf(determination.substring("origin:".length()), getTagContext(path));
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
            LocationTag new_to = LocationTag.valueOf(determination.substring("destination:".length()), getTagContext(path));
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
            LocationTag new_to = LocationTag.valueOf(determination, getTagContext(path));
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
        switch (name) {
            case "origin":
                return from;
            case "destination":
                return to;
            case "entity":
                return entity.getDenizenObject();
            case "cause":
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
        cause = event instanceof EntityPortalEvent ? "ENTITY_PORTAL" : "ENTITY_TELEPORT";
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
