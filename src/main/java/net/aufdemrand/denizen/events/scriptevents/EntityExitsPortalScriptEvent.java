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
import org.bukkit.event.entity.EntityPortalExitEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EntityExitsPortalScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity exits portal
    // <entity> exits portal
    //
    // @Cancellable false
    //
    // @Triggers when an entity enters a portal.
    //
    // @Context
    // <context.entity> returns the dEntity.
    // <context.location> returns the dLocation of the portal block touched by the entity.
    //
    // -->

    public EntityExitsPortalScriptEvent() {
        instance = this;
    }
    public static EntityExitsPortalScriptEvent instance;
    public dEntity entity;
    public dLocation location;
    public EntityPortalExitEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String entOne = CoreUtilities.getXthArg(0, lower);
        List<String> types = Arrays.asList("entity", "player", "npc");
        return (types.contains(entOne) || dEntity.matches(entOne))
                && lower.contains("exits portal");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String target = CoreUtilities.getXthArg(0,lower);
        List<String> types = Arrays.asList("entity", "player", "npc");
        return types.contains(target) || entity.matchesEntity(target);
    }

    @Override
    public String getName() {
        return "EntityExitsPortal";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityPortalExitEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()): null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()): null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("location", location);
        return context;
    }

    @EventHandler
    public void onEntityExitsPortal(EntityPortalExitEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(event.getTo());
        this.event = event;
        fire();
    }
}
