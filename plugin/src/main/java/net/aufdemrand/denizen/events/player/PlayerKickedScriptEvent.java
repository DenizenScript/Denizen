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

public class PlayerKickedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player kicked
    //
    // @Regex ^on player kicked$
    //
    // @Cancellable true
    //
    // @Triggers when a player is kicked from the server.
    //
    // @Context
    // <context.message> returns an Element of the kick message sent to all players.
    // <context.reason> returns an Element of the kick reason.
    //
    // @Determine
    // "MESSAGE:" + Element to change the kick message.
    // "REASON:" + Element to change the kick reason.
    //
    // -->

    public PlayerKickedScriptEvent() {
        instance = this;
    }

    public static PlayerKickedScriptEvent instance;
    public Element message;
    public Element reason;
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
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("message:")) {
            message = new Element(lower.substring(8));
            return true;
        }
        else if (lower.startsWith("reason:")) {
            reason = new Element(lower.substring(7));
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("message")) {
            return message;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        message = new Element(event.getLeaveMessage());
        reason = new Element(event.getReason());
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setLeaveMessage(message.asString());
        event.setReason(reason.asString());
        event.setCancelled(cancelled);

    }
}
