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
import org.bukkit.event.player.PlayerKickEvent;

import java.util.HashMap;

public class PlayerKickedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player kicked
    //
    // @Cancellable true
    //
    // @Triggers when a player kicked the server.
    //
    // @Context
    // <context.message> returns an Element of the kicked message.
    //
    // @Determine
    // Element(String) to change the kicked message.
    //
    // -->

    public PlayerKickedScriptEvent() {
        instance = this;
    }

    public static PlayerKickedScriptEvent instance;
    public String message;
    public PlayerKickEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player kicked");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerKicked";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerKickEvent.getHandlerList().unregister(this);
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

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        message = event.getLeaveMessage();
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
        event.setLeaveMessage(message);

    }
}
