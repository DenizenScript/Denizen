package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerDeepSleepScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player deep sleeps
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a player has slept long enough in a bed to count as being in deep sleep and thus skip the night. Cancelling the event prevents the player from qualifying to skip the night.
    //
    // @Player Always.
    //
    // -->

    public PlayerDeepSleepScriptEvent() {
        registerCouldMatcher("player deep sleeps");
    }

    public PlayerDeepSleepEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @EventHandler
    public void playerDeepSleep(PlayerDeepSleepEvent event) {
        this.event = event;
        fire(event);
    }
}
