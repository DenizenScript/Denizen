package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerSetSpawnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player sets spawn
    //
    // @Cancellable true
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Triggers when a player's spawn point changes.
    //
    // @Switch cause:<cause> to only process when the cause for the event matches the input.
    //
    // @Context
    // <context.cause> returns the reason the player's spawn point changed. A list of causes can be found at <@link url https://jd.papermc.io/paper/1.21.5/com/destroystokyo/paper/event/player/PlayerSetSpawnEvent.Cause.html>.
    // <context.forced> returns whether this event will persist through source block (bed or respawn anchor) removal.
    // <context.location> returns a LocationTag of the new respawn location, if any.
    // <context.message> returns the notification message that is sent to the player.
    // <context.notify> returns whether the player will be notified their spawn point changed.
    //
    // @Determine
    // "FORCED:<ElementTag(Boolean)>" to set whether the player's new spawnpoint will persist even if the bed or respawn anchor that triggered the event is removed.
    // "MESSAGE:<ElementTag>" to set the notification message that is sent to the player.
    // "NOTIFY:<ElementTag(Boolean)>" to set whether the player will be notified their spawnpoint changed.
    // LocationTag to change the respawn location.
    //
    // @Player Always.
    //
    // -->

    public PlayerSetSpawnScriptEvent() {
        registerCouldMatcher("player sets spawn");
        registerSwitches("cause");
        this.<PlayerSetSpawnScriptEvent, ElementTag>registerOptionalDetermination("forced", ElementTag.class, (evt, context, value) -> {
            if (value.isBoolean()) {
                evt.event.setForced(value.asBoolean());
                return true;
            }
            return false;
        });
        this.<PlayerSetSpawnScriptEvent, ElementTag>registerDetermination("message", ElementTag.class, (evt, context, message) -> {
            evt.event.setNotification(PaperModule.parseFormattedText(message.toString(), ChatColor.WHITE));
        });
        this.<PlayerSetSpawnScriptEvent, ElementTag>registerOptionalDetermination("notify", ElementTag.class, (evt, context, value) -> {
            if (value.isBoolean()) {
                evt.event.setNotifyPlayer(value.asBoolean());
                return true;
            }
            return false;
        });
        this.<PlayerSetSpawnScriptEvent, LocationTag>registerDetermination(null, LocationTag.class, (evt, context, location) -> {
            evt.event.setLocation(location);
            evt.event.setForced(true); // required if the cause is a bed or respawn anchor
        });
    }

    public PlayerSetSpawnEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getLocation())) {
            return false;
        }
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
            case "location" -> event.getLocation() != null ? new LocationTag(event.getLocation()) : null;
            case "message" -> event.getNotification() != null ? new ElementTag(PaperModule.stringifyComponent(event.getNotification()), true) : null;
            case "notify" -> new ElementTag(event.willNotifyPlayer());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerSetsSpawn(PlayerSetSpawnEvent event) {
        this.event = event;
        fire(event);
    }
}
