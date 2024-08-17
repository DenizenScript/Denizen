package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerShieldDisableScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player shield disables
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers When a player shield is disabled.
    //
    // @Context
    // <context.damager> returns an EntityTag of the attacker.
    // <context.cooldown> returns a DurationTag of the cooldown.
    //
    // @Determine
    // "COOLDOWN:<DurationTag>" to change the cooldown.
    //
    // @Player Always.
    //
    // -->

    public PlayerShieldDisableScriptEvent() {
        registerCouldMatcher("player shield disables");
        this.<PlayerShieldDisableScriptEvent, DurationTag>registerDetermination("cooldown", DurationTag.class, (evt, context, duration) -> {
            evt.event.setCooldown(duration.getTicksAsInt());
        });
    }

    public PlayerShieldDisableEvent event;
    public LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "damager" -> new EntityTag(event.getDamager()).getDenizenObject();
            case "cooldown" -> new DurationTag((long) event.getCooldown());
            default -> super.getContext(name);
        };
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @EventHandler
    public void playerShieldDisableEvent(PlayerShieldDisableEvent event) {
        location = new LocationTag(event.getPlayer().getLocation());
        this.event = event;
        fire(event);
    }
}
