package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class PlayerSprintScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player toggles|starts|stops sprinting
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player starts or stops sprinting.
    //
    // @Context
    // <context.state> returns an ElementTag(Boolean) with a value of "true" if the player is now sprinting and "false" otherwise.
    //
    // @Player Always.
    //
    // -->

    public PlayerSprintScriptEvent() {
        registerCouldMatcher("player toggles|starts|stops sprinting");
    }

    public boolean state;
    public PlayerToggleSprintEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (cmd.equals("starts") && !state) {
            return false;
        }
        if (cmd.equals("stops") && state) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
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
        if (name.equals("state")) {
            return new ElementTag(state);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerSprint(PlayerToggleSprintEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        state = event.isSprinting();
        this.event = event;
        fire(event);
    }
}
