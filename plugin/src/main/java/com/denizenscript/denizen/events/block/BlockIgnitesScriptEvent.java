package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class BlockIgnitesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block ignites
    // <material> ignites
    //
    // @Regex ^on [^\s]+ ignites$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block is set on fire.
    //
    // @Context
    // <context.location> returns the LocationTag of the block was set on fire at.
    // <context.material> returns the MaterialTag of the block that was set on fire.
    // <context.entity> returns the EntityTag of the entity that ignited the block.
    // <context.origin_location> returns the LocationTag of the fire block that ignited this block.
    // <context.cause> returns an ElementTag of the cause of the event: ENDER_CRYSTAL, EXPLOSION, FIREBALL, FLINT_AND_STEEL, LAVA, or SPREAD.
    //
    // -->

    public BlockIgnitesScriptEvent() {
        instance = this;
    }

    public static BlockIgnitesScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public EntityTag entity;
    public LocationTag origin_location;
    public ElementTag cause;
    public BlockIgniteEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("ignites");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }

        String mat = path.eventArgLowerAt(0);
        return tryMaterial(material, mat);
    }

    @Override
    public String getName() {
        return "BlockIgnites";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ObjectTag getContext(String name) {
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
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        entity = null;
        if (event.getIgnitingEntity() != null) {
            entity = new EntityTag(event.getIgnitingEntity());
        }
        origin_location = null;
        if (event.getIgnitingBlock() != null) { // TODO: Why would this be null?
            origin_location = new LocationTag(event.getIgnitingBlock().getLocation());
        }
        cause = new ElementTag(event.getCause().toString());
        this.event = event;
        fire(event);
    }
}
