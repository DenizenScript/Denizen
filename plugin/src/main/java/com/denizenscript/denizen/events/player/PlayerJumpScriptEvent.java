package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerJumpScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player jumps
    //
    // @Regex ^on player jumps$
    // @Switch in <area>
    //
    // @Triggers when a player jumps.
    //
    // @Context
    // <context.location> returns the location the player jumped from (not particularly accurate).
    //
    // -->

    public PlayerJumpScriptEvent() {
        instance = this;
    }

    public static PlayerJumpScriptEvent instance;

    public dLocation location;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player jumps");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getFrom())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerJumps";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(event != null ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerJumps(PlayerMoveEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        // Check that the block level changed (Upward)
        if (event.getTo().getBlockY() > event.getFrom().getBlockY()
                // and also that the player has a high velocity (jump instead of walking up stairs)
                && Math.abs(event.getPlayer().getVelocity().getY()) > 0.1
                // and that the player isn't in any form of fast moving vehicle
                && event.getPlayer().getVehicle() == null) {
            // Not perfect checking, but close enough until Bukkit adds a proper event
            location = new dLocation(event.getFrom());
            this.event = event;
            fire(event);
        }
    }
}
