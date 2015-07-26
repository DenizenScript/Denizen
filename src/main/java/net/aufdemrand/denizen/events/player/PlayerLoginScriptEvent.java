package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;

public class PlayerLoginScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player logs in (for the first time)
    // player (first) login
    //
    // @Regex ^on player (logs in( for the first time)?|( first)? login)$
    //
    // @Cancellable false
    //
    // @Triggers when a player logs in to the server.
    //
    // @Context
    // <context.hostname> returns an Element of the player's hostname.
    //
    // @Determine
    // "KICKED" to kick the player from the server.
    // "KICKED Element(String)" to kick the player and specify a message to show.
    //
    // -->

    public PlayerLoginScriptEvent() {
        instance = this;
    }

    public static PlayerLoginScriptEvent instance;
    private String message;
    private Boolean kicked;
    public PlayerLoginEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player") && (lower.contains("logs in") || lower.contains("login"));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        if (CoreUtilities.toLowerCase(s).contains("first") && dPlayer.isNoted(event.getPlayer())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerLogin";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerLoginEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (CoreUtilities.toLowerCase(determination).startsWith("kicked")) {
            message = determination.length() > 7 ? determination.substring(7) : determination;
            kicked = true;
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("hostname", new Element(event.getAddress().toString()));
        return context;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        kicked = false;
        this.event = event;
        fire();
        if (kicked) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, message);
        }
    }
}
