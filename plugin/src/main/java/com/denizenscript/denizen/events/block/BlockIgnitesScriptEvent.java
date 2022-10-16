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
    //
    // @Group Block
    //
    // @Location true
    // @Switch cause:<cause> to only process the event when it came from a specified cause.
    //
    // @Cancellable true
    //
    // @Triggers when a block is set on fire.
    //
    // @Context
    // <context.location> returns the LocationTag of the block that was set on fire.
    // <context.entity> returns the EntityTag of the entity that ignited the block (if any).
    // <context.origin_location> returns the LocationTag of the fire block that ignited this block (if any).
    // <context.cause> returns an ElementTag of the cause of the event: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockIgniteEvent.IgniteCause.html>.
    //
    // -->

    public BlockIgnitesScriptEvent() {
        registerCouldMatcher("block ignites");
        registerSwitches("cause");
    }

    public LocationTag location;
    public MaterialTag material;
    public ElementTag cause;
    public BlockIgniteEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "cause", cause.asString())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "material": return new MaterialTag(event.getBlock());
            case "cause": return cause;
            case "entity":
                if (event.getIgnitingEntity() != null) {
                    return new EntityTag(event.getIgnitingEntity()).getDenizenObject();
                }
                break;
            case "origin_location":
                if (event.getIgnitingBlock() != null) {
                    return new LocationTag(event.getIgnitingBlock().getLocation());
                }
                break;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockIgnites(BlockIgniteEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        cause = new ElementTag(event.getCause());
        this.event = event;
        fire(event);
    }
}
