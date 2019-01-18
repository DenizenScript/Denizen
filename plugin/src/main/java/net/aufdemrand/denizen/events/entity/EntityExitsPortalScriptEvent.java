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
import org.bukkit.event.entity.EntityPortalExitEvent;

public class EntityExitsPortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity exits portal (in <area>)
    // <entity> exits portal (in <area>)
    //
    // @Regex ^on [^\s]+ exits portal( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Triggers when an entity exits a portal.
    //
    // @Context
    // <context.entity> returns the dEntity.
    // <context.location> returns the dLocation of the portal block touched by the entity.
    //
    // @Player when the entity that exits the portal is a player.
    //
    // @NPC when the entity that exits the portal is an NPC.
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
        return CoreUtilities.toLowerCase(s).contains("exits portal");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String target = CoreUtilities.getXthArg(0, lower);

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
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

    @EventHandler
    public void onEntityExitsPortal(EntityPortalExitEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(event.getTo());
        this.event = event;
        fire();
    }
}
