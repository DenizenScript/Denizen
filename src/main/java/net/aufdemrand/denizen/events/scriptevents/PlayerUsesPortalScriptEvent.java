package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PlayerUsesPortalScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player uses portal
    //
    // @Cancellable false
    //
    // @Triggers when a player enters a portal.
    //
    // @Context
    // <context.from> returns the location teleported from.
    // <context.to> returns the location teleported to.
    //
    // @Determine
    // dLocation to change the destination.
    // -->

    public PlayerUsesPortalScriptEvent() {
        instance = this;
    }
    public static PlayerUsesPortalScriptEvent instance;
    public dEntity entity;
    public dLocation to;
    public dLocation from;
    public PlayerPortalEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).contains("player uses portal");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerUsesPortal";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerPortalEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (dLocation.matches(determination)) {
            to = dLocation.valueOf(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getPlayer()): null, null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("to", to);
        context.put("from", from);
        return context;
    }

    @EventHandler
    public void onEntityEntersPortal(PlayerPortalEvent event) {
        entity = new dEntity(event.getPlayer());
        to = new dLocation(event.getTo());
        from = new dLocation(event.getFrom());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setTo(to);
    }
}
