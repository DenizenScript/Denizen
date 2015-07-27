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
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class PlayerQuitsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player quits
    // player quit
    //
    // @Regex ^on player (quit|quits)$
    //
    // @Cancellable false
    //
    // @Triggers when a player quit the server.
    //
    // @Context
    // <context.message> returns an Element of the quit message.
    //
    // @Determine
    // Element(String) to change the quit message.
    //
    // -->

    public PlayerQuitsScriptEvent() {
        instance = this;
    }

    public static PlayerQuitsScriptEvent instance;
    public String message;
    public PlayerQuitEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player quit");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerQuits";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (!CoreUtilities.toLowerCase(determination).equals("none")) {
            message = determination;
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
        context.put("message", new Element(message));
        return context;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuits(PlayerQuitEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        message = event.getQuitMessage();
        this.event = event;
        fire();
        event.setQuitMessage(message);

    }
}
