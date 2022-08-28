package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.world.RaidScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidTriggerEvent;

public class PlayerTriggersRaidScriptEvent extends RaidScriptEvent<RaidTriggerEvent> implements Listener {

    // <--[event]
    // @Events
    // player triggers raid
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player triggers a village raid.
    //
    // @Context
    // <context.raid> returns the raid data. See <@link language Raid Event Data>.
    //
    // @Player Always.
    //
    // -->

    public PlayerTriggersRaidScriptEvent() {
        super(false);
        registerCouldMatcher("player triggers raid");
    }

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
    public void onPlayerTriggersRaid(RaidTriggerEvent event) {
        this.event = event;
        fire(event);
    }
}
