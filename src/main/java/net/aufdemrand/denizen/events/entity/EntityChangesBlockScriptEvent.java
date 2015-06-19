package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EntityChangesBlockScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity changes block
    // entity changes block (into <material>) (in <notable cuboid>)
    // entity changes <material> (into <material>) (in <notable cuboid>)
    // <entity> changes block (into <material>) (in <notable cuboid>)
    // <entity> changes <material> (into <material>) (in <notable cuboid>)
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
        String entOne = CoreUtilities.getXthArg(0, lower);
        String mat = CoreUtilities.getXthArg(2, lower);
        List<String> types = Arrays.asList("entity", "player", "npc");
        return CoreUtilities.xthArgEquals(1, lower, "changes")
                && (types.contains(entOne) || dEntity.matches(entOne))
                && (mat.equals("block") || dMaterial.matches(mat));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String entName = CoreUtilities.getXthArg(0, lower);
        if (!entity.matchesEntity(entName)){
            return false;
        }

        String notable = null;
        if (CoreUtilities.xthArgEquals(3, lower, "in")) {
            notable = CoreUtilities.getXthArg(4, lower);
        }
        else if (CoreUtilities.xthArgEquals(5, lower, "in")) {
            notable = CoreUtilities.getXthArg(6, lower);
        }
        if (notable != null) {
            if (dCuboid.matches(notable)) {
                dCuboid cuboid = dCuboid.valueOf(notable);
                if (!cuboid.isInsideCuboid(location)) {
                    return false;
                }
            }
            else if (dEllipsoid.matches(notable)) {
                dEllipsoid ellipsoid = dEllipsoid.valueOf(notable);
                if (!ellipsoid.contains(location)) {
                    return false;
                }
            }
            else {
                dB.echoError("Invalid event 'IN ...' check [" + getName() + "]: '" + s + "' for " + scriptContainer.getName());
                return false;
            }
        }

        if (CoreUtilities.xthArgEquals(3, lower, "into")) {
            String mat = CoreUtilities.getXthArg(4, lower);
            if (!mat.equals("block") && !mat.equals(new_material.identifyFullNoIdentifier())) {
                return false;
            }
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
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()): null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()): null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("cuboids", cuboids);
        context.put("location", location);
        context.put("new_material", new_material);
        context.put("old_material", old_material);
        return context;
    }

    @EventHandler
    public void onEntityChangesBlock(EntityChangeBlockEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(event.getBlock().getLocation());
        old_material = dMaterial.getMaterialFrom(location.getBlock().getType(), location.getBlock().getData());
        new_material = dMaterial.getMaterialFrom(event.getTo());
        cuboids = new dList();
        for (dCuboid cuboid: dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
