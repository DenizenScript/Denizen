package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerUsesPortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player uses portal
    //
    // @Regex ^on player uses portal$
    //
    // @Switch in <area>
    //
    // @Triggers when a player enters a portal.
    //
    // @Context
    // <context.from> returns the location teleported from.
    // <context.to> returns the location teleported to.
    //
    // @Determine
    // LocationTag to change the destination.
    //
    // @Player Always.
    //
    // -->

    public PlayerUsesPortalScriptEvent() {
        instance = this;
    }

    public static PlayerUsesPortalScriptEvent instance;
    public EntityTag entity;
    public LocationTag to;
    public LocationTag from;
    public PlayerPortalEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player uses portal");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return runInCheck(path, to) || runInCheck(path, from);
    }

    @Override
    public String getName() {
        return "PlayerUsesPortal";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (LocationTag.matches(determination)) {
            to = LocationTag.valueOf(determination);
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("to")) {
            return to;
        }
        else if (name.equals("from")) {
            return from;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerEntersPortal(PlayerPortalEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        entity = new EntityTag(event.getPlayer());
        to = event.getTo() == null ? null : new LocationTag(event.getTo());
        from = new LocationTag(event.getFrom());
        this.event = event;
        fire(event);
        event.setTo(to);
    }
}
