package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player quits
    // player quit
    //
    // @Synonyms Player Disconnects,Player Logs Off,Player Leaves
    //
    // @Group Player
    //
    // @Switch cause:<cause> to only process the event when it matches the specific cause (only on Paper).
    //
    // @Location true
    //
    // @Triggers when a player quit the server.
    //
    // @Context
    // <context.message> returns an ElementTag of the quit message.
    // <context.cause> returns an ElementTag of the cause of the quit (only on Paper): <@link url https://jd.papermc.io/paper/1.21.1/org/bukkit/event/player/PlayerQuitEvent.QuitReason.html>.
    //
    // @Determine
    // ElementTag to change the quit message.
    // "NONE" to cancel the quit message.
    //
    // @Player Always.
    //
    // -->

    public PlayerQuitsScriptEvent() {
        registerCouldMatcher("player quits|quit");
        this.<PlayerQuitsScriptEvent>registerTextDetermination("none", (evt) -> {
            event.setQuitMessage(null);
        });
        this.<PlayerQuitsScriptEvent, ElementTag>registerOptionalDetermination(null, ElementTag.class, (evt, context, determination) -> {
            event.setQuitMessage(determination.asString());
            return true;
        });
    }

    public PlayerQuitEvent event;
    public LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
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
            case "message" -> new ElementTag(event.getQuitMessage());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerQuits(PlayerQuitEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        if (!event.getPlayer().isOnline()) { // Workaround: Paper event misfire - refer to comments in NetworkInterceptHelper
            return;
        }
        location = new LocationTag(event.getPlayer().getLocation());
        this.event = event;
        fire(event);

    }
}
