package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

public class PlayerWalkScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player walks
    //
    // @Warning This event fires very very rapidly!
    //
    // @Cancellable true
    //
    // @Triggers when a player moves in the slightest.
    //
    // @Context
    // <context.old_location> returns the location of where the player was.
    // <context.new_location> returns the location of where the player is.
    //
    // -->

    public PlayerWalkScriptEvent() {
        instance = this;
    }
    public static PlayerWalkScriptEvent instance;
    public dLocation old_location;
    public dLocation new_location;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.equals("player walks");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerWalks";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerMoveEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("old_location", old_location);
        context.put("new_location", new_location);
        return context;
    }

    @EventHandler
    public void onPlayerMoves(PlayerMoveEvent event) {
        old_location = new dLocation(event.getFrom());
        new_location = new dLocation(event.getTo());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
