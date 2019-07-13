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
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class PlayerChangesGamemodeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes gamemode (to <gamemode>)
    //
    // @Regex ^on player changes gamemode( to [^\s]+)?$
    //
    // @Cancellable true
    //
    // @Triggers when a player's gamemode is changed.
    //
    // @Context
    // <context.gamemode> returns an ElementTag of the gamemode.
    // Game Modes: <@link url http://bit.ly/1KHab43>
    // -->

    public PlayerChangesGamemodeScriptEvent() {
        instance = this;
    }

    public static PlayerChangesGamemodeScriptEvent instance;
    public ElementTag gamemode;
    public PlayerGameModeChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player changes gamemode");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mode = path.eventArgLowerAt(4);
        if (mode.length() > 0) {
            if (!CoreUtilities.toLowerCase(gamemode.asString()).equals(mode)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerChangesGamemode";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("gamemode")) {
            return gamemode;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesGamemode(PlayerGameModeChangeEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        gamemode = new ElementTag(event.getNewGameMode().name());
        this.event = event;
        fire(event);
    }
}
