package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;

public class EntityCombustsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity combusts (in <area>)
    // <entity> combusts (in <area>)
    //
    // @Regex ^on [^\s]+ combusts( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity catches fire.
    //
    // @Context
    // <context.entity> returns the entity that caught fire.
    // <context.duration> returns the length of the burn.
    // <context.source> returns the dEntity that caused the fire, if any. NOTE: If the source is a location, just use <context.entity.location> or <context.entity.location.above>
    // <context.source_type> returns the type of the source, which can be: ENTITY, LOCATION, NONE.
    //
    // @Determine
    // Element(Number) set the length of duration.
    //
    // @Player when the entity that catches fire is a player.
    //
    // @NPC when the entity that catches fire is an NPC.
    //
    // -->

    public EntityCombustsScriptEvent() {
        instance = this;
    }

    public static EntityCombustsScriptEvent instance;
    public dEntity entity;
    private int burntime;
    public EntityCombustEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("combusts");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!tryEntity(entity, CoreUtilities.getXthArg(0, lower))) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, entity.getLocation())) {
            return false;
        }

        return true;
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
        if (aH.matchesInteger(determination)) {
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
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("duration")) {
            return new Duration(burntime);
        }
        else if (name.equals("source")) {
            if (event instanceof EntityCombustByEntityEvent) {
                return new dEntity(((EntityCombustByEntityEvent) event).getCombuster());
            }
        }
        else if (name.equals("source_type")) {
            if (event instanceof EntityCombustByEntityEvent) {
                return new Element("ENTITY");
            }
            else if (event instanceof EntityCombustByBlockEvent) {
                return new Element("LOCATION");
            }
            return new Element("NONE");
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityCombusts(EntityCombustEvent event) {
        entity = new dEntity(event.getEntity());
        burntime = event.getDuration();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setDuration(burntime);
    }
}
