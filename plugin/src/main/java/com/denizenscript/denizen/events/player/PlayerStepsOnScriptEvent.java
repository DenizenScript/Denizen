package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerStepsOnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player steps on block
    // player steps on <material>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Warning This event may fire very rapidly.
    //
    // @Cancellable true
    //
    // @Triggers when a player steps onto a specific block material.
    //
    // @Context
    // <context.location> returns a LocationTag of the block the player is stepping on.
    // <context.previous_location> returns a LocationTag of where the player was before stepping onto the block.
    // <context.new_location> returns a LocationTag of where the player is now.
    //
    // @Player Always.
    //
    // -->

    public PlayerStepsOnScriptEvent() {
        registerCouldMatcher("player steps on <block>");
    }

    public LocationTag location;
    public PlayerMoveEvent event;
    public MaterialTag material;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(3, material)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "previous_location" -> new LocationTag(event.getFrom());
            case "new_location" -> new LocationTag(event.getTo());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerStepsOn(PlayerMoveEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        if (event.getTo() == null) {
            return;
        }
        Location from = event.getFrom().clone().subtract(0, 0.05, 0), to = event.getTo().clone().subtract(0, 0.05, 0);
        if (LocationTag.isSameBlock(from, to)) {
            return;
        }
        location = new LocationTag(to);
        if (!Utilities.isLocationYSafe(location)) {
            return;
        }
        material = new MaterialTag(location.getBlock());
        this.event = event;
        fire(event);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        onPlayerStepsOn(event);
    }
}
