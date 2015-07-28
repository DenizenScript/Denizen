package net.aufdemrand.denizen.events.entity;

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
import org.bukkit.event.entity.EntityPortalEnterEvent;

import java.util.HashMap;

public class EntityEntersPortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity enters portal (in <area>)
    // <entity> enters portal (in <area>)
    //
    // @Regex ^on [^\s]+ enters portal( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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

    public EntityEntersPortalScriptEvent() {
        instance = this;
    }

    public static EntityEntersPortalScriptEvent instance;
    public dEntity entity;
    public dLocation location;
    public EntityPortalEnterEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).contains("enters portal");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String target = CoreUtilities.getXthArg(0, lower);
        return entity.matchesEntity(target) && runInCheck(scriptContainer, s, lower, entity.getLocation());
    }

    @Override
    public String getName() {
        return "EntityEntersPortal";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityPortalEnterEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
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
        else if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityEntersPortal(EntityPortalEnterEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(event.getLocation());
        this.event = event;
        fire();
    }
}
