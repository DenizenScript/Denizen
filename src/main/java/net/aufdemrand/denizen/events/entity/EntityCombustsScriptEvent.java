package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EntityCombustsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity combusts
    // <entity> combusts
    //
    // @Cancellable true
    //
    // @Triggers when an entity catches fire.
    //
    // @Context
    // <context.entity> returns the entity that caught fire.
    // <context.duration> returns the length of the burn.
    //
    // @Determine
    // Element(Number) set the length of duration.
    //
    // -->

    public EntityCombustsScriptEvent() {
        instance = this;
    }

    public static EntityCombustsScriptEvent instance;
    public dEntity entity;
    public Duration duration;
    private Integer burntime;
    public EntityCombustEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        String entOne = CoreUtilities.getXthArg(0, lower);
        List<String> types = Arrays.asList("entity", "player", "npc");
        return (types.contains(entOne) || dEntity.matches(entOne))
                && cmd.equals("combusts");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String target = CoreUtilities.getXthArg(0, CoreUtilities.toLowerCase(s));
        List<String> types = Arrays.asList("entity", "player", "npc");
        return (types.contains(target) || entity.matchesEntity(target));
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
        if (aH.Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Integer)) {
            burntime = aH.getIntegerFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("duration", duration);
        return context;
    }

    @EventHandler
    public void onEntityCombusts(EntityCombustEvent event) {
        entity = new dEntity(event.getEntity());
        duration = new Duration(event.getDuration());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setDuration(burntime);
    }
}
