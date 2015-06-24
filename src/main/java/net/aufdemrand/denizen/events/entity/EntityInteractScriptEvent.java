package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
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
import org.bukkit.event.entity.EntityInteractEvent;

import java.util.HashMap;

public class EntityInteractScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> interacts with <material> (in <area>)
    // entity interacts with <material> (in <area>)
    // <entity> interacts with block (in <area>)
    // entity interacts with block (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when an entity interacts with a block (EG an arrow hits a button)
    //
    // @Context
    // <context.location> returns a dLocation of the block being interacted with.
    // <context.cuboids> returns a dList of all cuboids the block is inside. DEPRECATED.
    // <context.entity> returns a dEntity of the entity doing the interaction.
    //
    // -->

    public EntityInteractScriptEvent() {
        instance = this;
    }
    public static EntityInteractScriptEvent instance;
    public dEntity entity;
    public dLocation location;
    public dList cuboids;
    public EntityInteractEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains("interacts with");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!entity.matchesEntity(CoreUtilities.getXthArg(0, lower))) {
            return false;
        }

        String mat = CoreUtilities.getXthArg(2, lower);
        if (mat.length() == 0) {
            dB.echoError("Invalid event material [" + getName() + "]: '" + s + "' for " + scriptContainer.getName());
            return false;
        }
        else if (!mat.equals("block") && !mat.equals(dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData()).identifySimpleNoIdentifier())) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityInteracts";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityInteractEvent.getHandlerList().unregister(this);
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
        context.put("cuboids", cuboids);
        return context;
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(event.getBlock().getLocation());
        for (dCuboid cuboid: dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }

}
