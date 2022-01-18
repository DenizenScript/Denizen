package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    // @Regex ^on player steps on [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Warning This event may fire very rapidly.
    //
    // @Cancellable true
    //
    // @Triggers when a player steps onto a material.
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
        instance = this;
    }

    public static PlayerStepsOnScriptEvent instance;
    public LocationTag location;
    public LocationTag previous_location;
    public LocationTag new_location;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player steps on")) {
            return false;
        }
        if (!couldMatchBlock(path.eventArgLowerAt(3))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(3);
        MaterialTag material = new MaterialTag(location.getBlock());
        if (!tryMaterial(material, mat)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerStepsOn";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location":
                return location;
            case "previous_location":
                return previous_location;
            case "new_location":
                return new_location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerStepsOn(PlayerMoveEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        if (LocationTag.isSameBlock(event.getFrom(), event.getTo())) {
            return;
        }
        location = new LocationTag(event.getTo().clone().subtract(0, 1, 0));
        if (!Utilities.isLocationYSafe(location)) {
            return;
        }
        previous_location = new LocationTag(event.getFrom());
        new_location = new LocationTag(event.getTo());
        this.event = event;
        fire(event);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        PlayerMoveEvent evt = new PlayerMoveEvent(event.getPlayer(), event.getFrom(), event.getTo());
        onPlayerStepsOn(evt);
        event.setCancelled(evt.isCancelled());
    }
}
