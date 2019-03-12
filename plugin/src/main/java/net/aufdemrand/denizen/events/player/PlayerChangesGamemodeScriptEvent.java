package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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
    // <context.gamemode> returns an Element of the gamemode.
    // Game Modes: <@link url http://bit.ly/1KHab43>
    // -->

    public PlayerChangesGamemodeScriptEvent() {
        instance = this;
    }

    public static PlayerChangesGamemodeScriptEvent instance;
    public Element gamemode;
    public PlayerGameModeChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player changes gamemode");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        String mode = CoreUtilities.getXthArg(4, lower);
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
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("gamemode")) {
            return gamemode;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesGamemode(PlayerGameModeChangeEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        gamemode = new Element(event.getNewGameMode().name());
        this.event = event;
        fire(event);
    }
}
