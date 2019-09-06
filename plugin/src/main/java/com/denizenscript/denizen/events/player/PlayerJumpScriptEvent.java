package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
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
    //
    // @Switch in <area>
    //
    // @Triggers when a player jumps.
    //
    // @Warning On Spigot servers, this event and its data are inaccurate and unreliable. This event works more stably on Paper servers.
    //
    // @Context
    // <context.location> returns the location the player jumped from.
    //
    // -->

    public PlayerJumpScriptEvent() {
        instance = this;
    }

    public static PlayerJumpScriptEvent instance;

    public LocationTag location;
    public PlayerTag player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player jumps");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerJumps";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    public static class PlayerJumpsSpigotScriptEventImpl extends PlayerJumpScriptEvent {

        @EventHandler
        public void onPlayerJumps(PlayerMoveEvent event) {
            if (EntityTag.isNPC(event.getPlayer())) {
                return;
            }
            // Check that the block level changed (Upward)
            if (event.getTo().getBlockY() > event.getFrom().getBlockY()
                    // and also that the player has a high velocity (jump instead of walking up stairs)
                    && Math.abs(event.getPlayer().getVelocity().getY()) > 0.1
                    // and that the player isn't in any form of fast moving vehicle
                    && event.getPlayer().getVehicle() == null) {
                // Not perfect checking, but close enough until Bukkit adds a proper event
                location = new LocationTag(event.getFrom());
                player = new PlayerTag(event.getPlayer());
                fire(event);
            }
        }
    }
}
