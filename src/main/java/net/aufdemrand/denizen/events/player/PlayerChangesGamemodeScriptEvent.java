package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import java.util.HashMap;

public class PlayerChangesGamemodeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes gamemode (to <gamemode>)
    //
    // @Regex ^on player changes gamemode( to [^\s]+)?$
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
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
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerGameModeChangeEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("gamemode")) {
            return gamemode;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangesGamemode(PlayerGameModeChangeEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        gamemode = new Element(event.getNewGameMode().name());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
