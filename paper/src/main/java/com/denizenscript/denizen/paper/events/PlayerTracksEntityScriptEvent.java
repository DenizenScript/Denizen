package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import io.papermc.paper.event.player.PlayerUntrackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTracksEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player tracks|untracks <entity>
    //
    // @Location true
    //
    // @Group Paper
    //
    // @Plugin Paper
    //
    // @Warning This event may fire very rapidly.
    //
    // @Triggers when a player starts or stop tracking an entity.
    //
    // @Context
    // <context.entity> returns an EntityTag of the entity being tracked.
    // <context.location> returns a LocationTag of the entity.
    //
    // @Player Always.
    //
    // @Example
    // # Narrate when the player tracks all entities except for item frames.
    // on player tracks !item_frame:
    // - narrate "You are now tracking <context.entity.name> at <context.location.simple>"
    //
    // @Example
    // on player untracks chicken:
    // - narrate "CHICKEN: No! Come back! :("
    // -->

    public PlayerTracksEntityScriptEvent() {
        registerCouldMatcher("player tracks|untracks <entity>");
    }

    public String type;
    public Player player;
    public EntityTag entity;
    public LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.eventArgLowerAt(1).equals(type)) {
            return false;
        }
        if (!path.tryArgObject(2, entity)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "location" -> location;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void playerTracksEntityEvent(PlayerTrackEntityEvent event) {
        entity = new EntityTag(event.getEntity());
        location = new LocationTag(event.getEntity().getLocation());
        player = event.getPlayer();
        type = "tracks";
        fire(event);
    }

    @EventHandler
    public void playerUntracksEntityEvent(PlayerUntrackEntityEvent event) {
        entity = new EntityTag(event.getEntity());
        location = new LocationTag(event.getEntity().getLocation());
        player = event.getPlayer();
        type = "untracks";
        fire(event);
    }
}
