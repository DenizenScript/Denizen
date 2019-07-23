package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreateScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // portal created (because <reason>)
    //
    // @Regex ^on portal created( because [^\s]+)?$
    //
    // @Group World
    //
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a portal is created.
    //
    // @Context
    // <context.world> returns the WorldTag the portal was created in.
    // <context.reason> returns an ElementTag of the reason the portal was created. (FIRE or OBC_DESTINATION)
    // <context.blocks> returns a ListTag of all the blocks that will become portal blocks.
    //
    // -->

    public PortalCreateScriptEvent() {
        instance = this;
    }

    public static PortalCreateScriptEvent instance;
    public ElementTag reason;
    public PortalCreateEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("portal created");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericCheck(path.eventArgLowerAt(2).equals("because") ? path.eventArgLowerAt(3) : path.eventArgLowerAt(5), reason.asString())) {
            return false;
        }
        if (!runInCheck(path, event.getBlocks().get(0).getLocation())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PortalCreate";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("world")) {
            return new WorldTag(event.getWorld());
        }
        else if (name.equals("reason")) {
            return reason;
        }
        else if (name.equals("blocks")) {
            ListTag blocks = new ListTag();
            for (Location location : NMSHandler.getInstance().getBlockHelper().getBlocksList(event)) {
                blocks.addObject(new LocationTag(location));
            }
            return blocks;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        reason = new ElementTag(event.getReason().name());
        this.event = event;
        fire(event);
    }
}
