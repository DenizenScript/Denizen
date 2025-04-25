package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSpawnChangeEvent;

public class PlayerSpawnChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player spawn changes
    //
    // @Cancellable true
    //
    // @Location true
    //
    // @Group Player
    //
    // @Triggers when a player's spawn point changes.
    //
    // @Switch cause:<cause> to only process when the cause for the event matches the input.
    //
    // @Context
    // <context.cause> returns the reason the player's spawn point changed. A list of causes can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/PlayerSpawnChangeEvent.Cause.html>.
    // <context.forced> returns whether this event will persist through source block (bed or respawn anchor) removal.
    // <context.location> returns a LocationTag of the new respawn location, if any.
    //
    // @Determine
    // "FORCED" to persist even if the bed or respawn anchor that triggered the event is removed.
    // LocationTag to change the respawn location.
    //
    // @Player Always.
    //
    // -->

    public PlayerSpawnChangeScriptEvent() {
        registerCouldMatcher("player spawn changes");
        registerSwitches("cause");
        this.<PlayerSpawnChangeScriptEvent>registerTextDetermination("forced", (evt) -> {
            evt.event.setForced(true);
        });
        this.<PlayerSpawnChangeScriptEvent, LocationTag>registerDetermination(null, LocationTag.class, (evt, context, location) -> {
            evt.event.setNewSpawn(location);
            evt.event.setForced(true); // required if the cause is a bed or respawn anchor
        });
    }

    public PlayerSpawnChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "cause", event.getCause().toString())) {
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
            case "cause" -> new ElementTag(event.getCause());
            case "forced" -> new ElementTag(event.isForced());
            case "location" -> event.getNewSpawn() == null ? null : new LocationTag(event.getNewSpawn());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerSpawnChange(PlayerSpawnChangeEvent event) {
        this.event = event;
        fire(event);
    }
}
