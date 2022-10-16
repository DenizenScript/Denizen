package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreateScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // portal created (because <'reason'>)
    //
    // @Group World
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a portal is created.
    //
    // @Context
    // <context.entity> returns the EntityTag that created the portal.
    // <context.world> returns the WorldTag the portal was created in.
    // <context.reason> returns an ElementTag of the reason the portal was created. (FIRE, NETHER_PAIR, END_PLATFORM)
    // <context.blocks> returns a ListTag of all the blocks that will become portal blocks.
    //
    // -->

    public PortalCreateScriptEvent() {
        registerCouldMatcher("portal created (because <'reason'>)");
    }

    public ElementTag reason;
    public PortalCreateEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventArgLowerAt(2).equals("because") && !runGenericCheck(path.eventArgLowerAt(3), reason.asString())) {
            return false;
        }
        if (!runInCheck(path, event.getBlocks().get(0).getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity": return event.getEntity() != null ? new EntityTag(event.getEntity()).getDenizenObject() : null;
            case "world": return new WorldTag(event.getWorld());
            case "reason": return reason;
            case "blocks":
                ListTag blocks = new ListTag();
                for (BlockState blockState : event.getBlocks()) {
                    blocks.addObject(new LocationTag(blockState.getLocation()));
                }
                return blocks;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        reason = new ElementTag(event.getReason());
        this.event = event;
        fire(event);
    }
}
