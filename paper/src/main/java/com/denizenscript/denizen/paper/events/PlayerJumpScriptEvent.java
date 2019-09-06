package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;

public class PlayerJumpScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // paper player jumps
    //
    // @Regex ^on paper player jumps$
    //
    // @Cancellable true
    //
    // @Switch in <area>
    //
    // @Plugin Paper
    //
    // @Triggers when a player jumps.
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
    public PlayerJumpEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("paper player jumps");
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
        return "PaperPlayerJumps";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event != null ? EntityTag.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerJumps(PlayerJumpEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        location = new LocationTag(event.getFrom());
        this.event = event;
        fire(event);
    }
}
