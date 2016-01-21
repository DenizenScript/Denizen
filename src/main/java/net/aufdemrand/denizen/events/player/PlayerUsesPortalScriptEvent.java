package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerUsesPortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player uses portal (in <area>)
    //
    // @Regex ^on player uses portal( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
        return CoreUtilities.toLowerCase(s).startsWith("player uses portal");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return runInCheck(scriptContainer, s, lower, to) || runInCheck(scriptContainer, s, lower, from);
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
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("to")) {
            return to;
        }
        else if (name.equals("from")) {
            return from;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerEntersPortal(PlayerPortalEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
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
