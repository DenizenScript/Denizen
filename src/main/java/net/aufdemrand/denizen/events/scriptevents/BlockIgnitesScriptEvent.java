package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

import java.util.HashMap;

public class BlockIgnitesScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block ignites
    // <material> ignites
    //
    // @Cancellable true
    //
    // @Triggers when a block is set on fire.
    //
    // @Context
    // <context.location> returns the dLocation the block was set on fire at.
    // <context.material> returns the dMaterial of the block that was set on fire.
    // <context.entity> returns the dEntity of the entity that ignited the block.
    // <context.origin_location> returns the dLocation of the fire block that ignited this block.
    // <context.cause> returns an Element of the cause of the event: ENDER_CRYSTAL, EXPLOSION, FIREBALL, FLINT_AND_STEEL, LAVA, or SPREAD.
    //
    // -->

    public BlockIgnitesScriptEvent() {
        instance = this;
    }
    public static BlockIgnitesScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public dEntity entity;
    public dLocation origin_location;
    public Element cause;
    public BlockIgniteEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String mat = CoreUtilities.getXthArg(0, lower);
        return lower.equals("block ignites")
                || (lower.equals(mat + " ignites") && dMaterial.matches(mat));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.equals("block ignites")
                || CoreUtilities.getXthArg(0,lower).equals(material.identifySimpleNoIdentifier());
    }

    @Override
    public String getName() {
        return "BlockIgnites";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockIgniteEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("material", material);
        if (entity != null) {
            context.put("entity", entity);
        }
        if (origin_location != null) {
            context.put("origin_location", origin_location);
        }
        context.put("cause", cause);
        return context;
    }

    @EventHandler
    public void onBlockIgnites(BlockIgniteEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        if (event.getIgnitingEntity() != null) {
            entity = new dEntity(event.getIgnitingEntity());
        }
        if (event.getIgnitingBlock() != null) {
            origin_location = new dLocation(event.getIgnitingBlock().getLocation());
        }
        cause = new Element(event.getCause().toString());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
