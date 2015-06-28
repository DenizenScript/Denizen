package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;

public class EntityTeleportScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity teleports
    // <entity> teleports
    //
    // @Triggers when an entity teleports.
    //
    // @Cancellable true
    //
    // @Context
    // <context.entity> returns the dEntity.
    // <context.origin> returns the dLocation the entity teleported from.
    // <context.destination> returns the dLocation the entity teleported to.
    // <context.cause> returns an Element of the teleport cause. Can be:
    // COMMAND, END_PORTAL, ENDER_PEARL, NETHER_PORTAL, PLUGIN, SPECTATE, UNKNOWN, or ENTITY_TELEPORT
    //
    // @Determine
    // "ORIGIN:" + dLocation to change the location the entity teleported from.
    // "DESTINATION:" + dLocation to change the location the entity teleports to.
    //
    // -->

    public EntityTeleportScriptEvent() {
        instance = this;
    }

    public static EntityTeleportScriptEvent instance;

    public dEntity entity;

    public dLocation from;

    public dLocation to;

    public String cause;

    public EntityTeleportEvent event;

    public PlayerTeleportEvent pEvent;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "teleports");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String ent = CoreUtilities.getXthArg(0, s);
        return entity.matchesEntity(ent);
    }

    @Override
    public String getName() {
        return "EntityTeleports";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityTeleportEvent.getHandlerList().unregister(this);
        PlayerTeleportEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String dlow = determination.toLowerCase();
        if (dlow.startsWith("origin:")) {
            dLocation new_from = dLocation.valueOf(determination.substring("origin:".length()));
            if (new_from != null) {
                from = new_from;
                return true;
            }
        }
        else if (dlow.startsWith("destination:")) {
            dLocation new_to = dLocation.valueOf(determination.substring("destination:".length()));
            if (new_to != null) {
                to = new_to;
                return true;
            }
        }
        else if (dLocation.matches(determination)) {
            dLocation new_to = dLocation.valueOf(determination);
            if (new_to != null) {
                to = new_to;
                return true;
            }
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(pEvent != null ? dEntity.getPlayerFrom(pEvent.getPlayer()) : null,
                entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("origin", from);
        context.put("destination", to);
        context.put("entity", entity.getDenizenObject());
        context.put("cause", new Element(cause));
        return context;
    }

    @EventHandler
    public void onEntityTeleports(EntityTeleportEvent event) {
        // TODO: cuboids?
        to = new dLocation(event.getTo());
        from = new dLocation(event.getFrom());
        entity = new dEntity(event.getEntity());
        cancelled = event.isCancelled();
        cause = "ENTITY_TELEPORT";
        this.event = event;
        pEvent = null;
        fire();
        event.setCancelled(cancelled);
        event.setFrom(from);
        event.setTo(to);
    }

    @EventHandler
    public void onPlayerTeleports(PlayerTeleportEvent event) {

        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }

        from = new dLocation(event.getFrom());
        to = new dLocation(event.getTo());
        entity = new dEntity(event.getPlayer());
        cancelled = event.isCancelled();
        cause = event.getCause().name();
        this.event = null;
        pEvent = event;
        fire();
        event.setCancelled(cancelled);
        event.setFrom(from);
        event.setTo(to);
    }
}
