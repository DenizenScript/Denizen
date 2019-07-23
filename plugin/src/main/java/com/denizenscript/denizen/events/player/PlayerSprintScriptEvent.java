package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class PlayerSprintScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player toggles sprinting
    // player starts sprinting
    // player stops sprinting
    //
    // @Regex ^on player (toggles|starts\stops) sprinting$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player starts or stops sprinting.
    //
    // @Context
    // <context.state> returns an Element(Boolean) with a value of "true" if the player is now sprinting and "false" otherwise.
    //
    // -->

    public PlayerSprintScriptEvent() {
        instance = this;
    }

    public static PlayerSprintScriptEvent instance;
    public Boolean state;
    public PlayerToggleSprintEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(2, CoreUtilities.toLowerCase(s)).startsWith("sprint");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (cmd.equals("starts") && !state) {
            return false;
        }
        if (cmd.equals("stops") && state) {
            return false;
        }

        return runInCheck(path, event.getPlayer().getLocation());
    }

    @Override
    public String getName() {
        return "PlayerSprints";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
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
