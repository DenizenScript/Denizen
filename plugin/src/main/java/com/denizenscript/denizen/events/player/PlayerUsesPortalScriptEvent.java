package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerUsesPortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player uses portal
    //
    // @Group Player
    //
    // @Location true
    //
    // @Switch from:<block> to only process the event if the block the player teleported from matches the LocationTag matcher provided.
    // @Switch to:<block> to only process the event if the block the player teleported to matches the LocationTag matcher provided.
    //
    // @Cancellable true
    //
    // @Triggers when a player enters a portal.
    //
    // @Context
    // <context.from> returns the location teleported from.
    // <context.to> returns the location teleported to (can sometimes be null).
    // <context.can_create> returns whether the server will attempt to create a destination portal.
    // <context.creation_radius> returns the radius that will be checked for a free space to create the portal in.
    // <context.search_radius> returns the radius that will be checked for an existing portal to teleport to.
    //
    // @Determine
    // LocationTag to change the destination.
    // "CAN_CREATE:<ElementTag(Boolean)>" to set whether the server will attempt to create a destination portal.
    // "CREATION_RADIUS:<ElementTag(Number)>" to set the radius that will be checked for a free space to create the portal in.
    // "SEARCH_RADIUS:<ElementTag(Number)>" to set the radius that will be checked for an existing portal to teleport to.
    //
    // @Player Always.
    //
    // -->

    public PlayerUsesPortalScriptEvent() {
        registerCouldMatcher("player uses portal");
        registerSwitches("from", "to");
    }

    public LocationTag to;
    public LocationTag from;
    public PlayerPortalEvent event;


    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, to) && !runInCheck(path, from)) {
            return false;
        }
        if (!path.tryObjectSwitch("from", from)) {
            return false;
        }
        if (!path.tryObjectSwitch("to", to)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = CoreUtilities.toLowerCase(determinationObj.toString());
            if (determination.startsWith("can_create:")) {
                event.setCanCreatePortal(new ElementTag(determination.substring("can_create:".length())).asBoolean());
                return true;
            }
            else if (determination.startsWith("creation_radius:")) {
                event.setCreationRadius(new ElementTag(determination.substring("creation_radius:".length())).asInt());
                return true;
            }
            else if (determination.startsWith("search_radius:")) {
                event.setSearchRadius(new ElementTag(determination.substring("search_radius:".length())).asInt());
                return true;
            }
        }
        if (determinationObj.canBeType(LocationTag.class)) {
            to = determinationObj.asType(LocationTag.class, getTagContext(path));
            event.setTo(to);
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "to": return to;
            case "from": return from;
            case "can_create": return new ElementTag(event.getCanCreatePortal());
            case "creation_radius": return new ElementTag(event.getCreationRadius());
            case "search_radius": return new ElementTag(event.getSearchRadius());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerEntersPortal(PlayerPortalEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        to = event.getTo() == null ? null : new LocationTag(event.getTo());
        from = new LocationTag(event.getFrom());
        this.event = event;
        fire(event);
    }
}
