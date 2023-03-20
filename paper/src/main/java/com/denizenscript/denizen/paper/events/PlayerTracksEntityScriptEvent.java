package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
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
    // @Triggers when a player starts or stops tracking an entity. An entity is tracked/untracked by a player's client when the player moves in/out of its <@link mechanism EntityTag.tracking_range>.
    //
    // @Context
    // <context.entity> returns an EntityTag of the entity being tracked or untracked.
    //
    // @Player Always.
    //
    // @Example
    // # Narrate when the player tracks any entities except for item frames.
    // on player tracks !item_frame:
    // - narrate "You are now tracking <context.entity.name> at <context.entity.location.simple>"
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

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals(type)) {
            return false;
        }
        if (!path.tryArgObject(2, entity)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
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
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerTracksEntityEvent(PlayerTrackEntityEvent event) {
        entity = new EntityTag(event.getEntity());
        player = event.getPlayer();
        type = "tracks";
        fire(event);
    }

    @EventHandler
    public void onPlayerUntracksEntityEvent(PlayerUntrackEntityEvent event) {
        entity = new EntityTag(event.getEntity());
        player = event.getPlayer();
        type = "untracks";
        EntityTag.rememberEntity(event.getEntity());
        fire(event);
        EntityTag.forgetEntity(event.getEntity());
    }
}
