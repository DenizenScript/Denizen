package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityChangesBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity changes block
    // entity changes block (into <material>) (in <area>)
    // entity changes <material> (into <material>) (in <area>)
    // <entity> changes block (into <material>) (in <area>)
    // <entity> changes <material> (into <material>) (in <area>)
    //
    // @Regex ^on [^\s]+ changes [^\s]+( into [^\s]+)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity changes the material of a block.
    //
    // @Context
    // <context.entity> returns the dEntity that changed the block.
    // <context.location> returns the dLocation of the changed block.
    // <context.old_material> returns the old material of the block.
    // <context.new_material> returns the new material of the block.
    //
    // @Player when the entity that changed the block is a player.
    //
    // -->

    public EntityChangesBlockScriptEvent() {
        instance = this;
    }

    public static EntityChangesBlockScriptEvent instance;
    public dEntity entity;
    public dLocation location;
    public dMaterial old_material;
    public dMaterial new_material;
    public dList cuboids;
    public EntityChangeBlockEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "changes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String entName = CoreUtilities.getXthArg(0, lower);

        if (!tryEntity(entity, entName)) {
            return false;
        }

        if (!tryMaterial(old_material, CoreUtilities.getXthArg(2, lower))) {
            return false;
        }

        if (CoreUtilities.xthArgEquals(3, lower, "into")) {
            String mat2 = CoreUtilities.getXthArg(4, lower);
            if (mat2.isEmpty()) {
                dB.echoError("Invalid event material [" + getName() + "]: '" + s + "' for " + scriptContainer.getName());
                return false;
            }
            else if (!tryMaterial(new_material, mat2)) {
                return false;
            }
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityChangesBlock";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityChangeBlockEvent.getHandlerList().unregister(this);
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
        else if (name.equals("cuboids")) { // NOTE: Deprecated
            return cuboids;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("new_material")) {
            return new_material;
        }
        else if (name.equals("old_material")) {
            return old_material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityChangesBlock(EntityChangeBlockEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(event.getBlock().getLocation());
        old_material = dMaterial.getMaterialFrom(location.getBlock().getType(), location.getBlock().getData());
        new_material = dMaterial.getMaterialFrom(event.getTo());
        cuboids = new dList();
        for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
