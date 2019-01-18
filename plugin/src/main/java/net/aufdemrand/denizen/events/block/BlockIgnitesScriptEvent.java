package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class BlockIgnitesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block ignites (in <area>)
    // <material> ignites (in <area>)
    //
    // @Regex ^on [^\s]+ ignites( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a block is set on fire.
    //
    // @Context
    // <context.location> returns the dLocation of the block was set on fire at.
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
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("ignites");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        String mat = CoreUtilities.getXthArg(0, lower);
        return tryMaterial(material, mat);
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
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        else if (name.equals("entity") && entity != null) {
            return entity;
        }
        else if (name.equals("origin_location") && origin_location != null) {
            return origin_location;
        }
        else if (name.equals("cause")) {
            return cause;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockIgnites(BlockIgniteEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        entity = null;
        if (event.getIgnitingEntity() != null) {
            entity = new dEntity(event.getIgnitingEntity());
        }
        origin_location = null;
        if (event.getIgnitingBlock() != null) { // TODO: Why would this be null?
            origin_location = new dLocation(event.getIgnitingBlock().getLocation());
        }
        cause = new Element(event.getCause().toString());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
