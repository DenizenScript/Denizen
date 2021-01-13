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
    // @Regex ^on [^\s]+ ignites$
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
        instance = this;
    }

    public static BlockIgnitesScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public ElementTag cause;
    public BlockIgniteEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("ignites")) {
            return false;
        }
        if (!couldMatchBlock(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

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
    public String getName() {
        return "BlockIgnites";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return new MaterialTag(event.getBlock());
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
        cause = new ElementTag(event.getCause().name());
        this.event = event;
        fire(event);
    }
}
