package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
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
    //
    // @Group Block
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    // @Switch cause:<cause> to only process the event when it came from a specified cause.
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
    public ElementTag cause;
    public BlockIgniteEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(1).equals("ignites");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "cause", cause.asString())) {
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
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        else if (name.equals("entity") && event.getIgnitingEntity() != null) {
            return new EntityTag(event.getIgnitingEntity());
        }
        else if (name.equals("origin_location") && event.getIgnitingBlock() != null) {
            return new LocationTag(event.getIgnitingBlock().getLocation());
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
        cause = new ElementTag(event.getCause().name());
        this.event = event;
        fire(event);
    }
}
