package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChoosesArrowScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player chooses arrow
    //
    // @Cancellable true
    //
    // @Group Player
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Triggers when a player chooses an arrow to load a bow/crossbow.
    //
    // @Switch arrow:<item> to only process the event when the players chosen arrow matches the input.
    // @Switch bow:<item> to only process the event when the players bow matches the input.
    //
    // @Context
    // <context.arrow> returns the ItemTag of the arrow that was chosen.
    // <context.bow> returns the ItemTag of the bow that is about to get loaded.
    //
    // @Player Always.
    //
    // @Example
    // # This example prevents using any arrow but spectral_arrows.
    // on player chooses arrow arrow:!spectral_arrow:
    // - determine cancelled
    // -->

    public PlayerChoosesArrowScriptEvent() {
        registerCouldMatcher("player chooses arrow");
        registerSwitches("arrow", "bow");
    }

    public PlayerReadyArrowEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("arrow", new ItemTag(event.getArrow()))) {
            return false;
        }
        if (!path.tryObjectSwitch("bow", new ItemTag(event.getBow()))) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "arrow": return new ItemTag(event.getArrow());
            case "bow": return new ItemTag(event.getBow());
        }
        return super.getContext(name);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public void cancellationChanged() {
        if (cancelled) {
            final Player p = event.getPlayer();
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), p::updateInventory, 1);
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onPlayerChoosesArrow(PlayerReadyArrowEvent event) {
        this.event = event;
        fire(event);
    }
}
