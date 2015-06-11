package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

import java.util.HashMap;

public class EntityCombustsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity combusts
    // <entity> combusts
    //
    // @Cancellable true
    //
    // @Triggers when an entity combusts.
    //
    // @Context
    // <context.duration> returns how long the entity takes to combust.
    // <context.entity> returns the dEntity that combusted.
    //
    // -->

    public EntityCombustsScriptEvent() {
        instance = this;
    }
    public static EntityCombustsScriptEvent instance;
    public dEntity entity;
    public Element duration;
    public EntityCombustEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains("combusts");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        return lower.equals("entity combusts")
                || lower.equals(entity.identifyType() + " combusts")
                || lower.equals(entity.identifySimple() + " combusts");
    }

    @Override
    public String getName() {
        return "EntityCombusts";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityCombustEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("duration", duration);
        return context;
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        entity = new dEntity(event.getEntity());
        duration = new Element(event.getDuration());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }

}
