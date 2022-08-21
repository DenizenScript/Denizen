package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
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
    // @Regex ^on player (quit|quits)$
    //
    // @Synonyms Player Disconnects,Player Logs Off,Player Leaves
    //
    // @Group Player
    //
    // @Triggers when a player quit the server.
    //
    // @Context
    // <context.message> returns an ElementTag of the quit message.
    //
    // @Determine
    // ElementTag to change the quit message.
    // "NONE" to cancel the quit message.
    //
    // @Player Always.
    //
    // -->

    public PlayerQuitsScriptEvent() {
    }

    public PlayerQuitEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player quit");
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            if (CoreUtilities.equalsIgnoreCase(determination, "none")) {
                event.setQuitMessage(null);
                return true;
            }
            event.setQuitMessage(determination);
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("message")) {
            return new ElementTag(event.getQuitMessage());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerQuits(PlayerQuitEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        if (!event.getPlayer().isOnline()) { // Workaround: Paper event misfire - refer to comments in NetworkInterceptHelper
            return;
        }
        this.event = event;
        fire(event);

    }
}
